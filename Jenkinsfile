pipeline {
    agent {
        docker {
            image 'omerapp99/custom-jenkins-agent4:latest'
            args '--rm --privileged -u 0:0 -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        SONAR_PROJECT_KEY = 'calc_jenkins'
        SONAR_PROJECT_NAME = 'calc_jenkins'
        FRONTEND_PORT = '3000'
        BACKEND_PORT = '5000'
        ECR_REPOSITORY_NAME = 'omerapp99/weather'
        AWS_REGION = 'eu-north-1'
        AWS_ACCOUNT_ID = '205337945058'
        EKS_CLUSTER_NAME = 'eks-cluster'
        S3FRONT = 'reactweatherapp'
        S3REPORTS = 'secscans'
        CRITICAL_THRESHOLD = '0'
    }
    
    parameters {
        booleanParam(name: 'RUN_STATIC_ANALYSIS', defaultValue: false, description: 'Run SonarQube static analysis')
        booleanParam(name: 'RUN_TESTS', defaultValue: false, description: 'Run Tests')
        booleanParam(name: 'RUN_IMAGE_SCAN', defaultValue: false, description: 'Run Image scan')
    }

    stages {
        stage('SBOM Scan') {
            steps {
                script {
                    env.TAG = env.BRANCH_NAME == 'master' ? "${env.BUILD_TAG}" : "${env.GIT_COMMIT}"
                    currentStage = 'Dependency Scan'
                    try {
                        sh '''
                        trivy fs --severity CRITICAL --output trivy_report_backend.txt ./backend
                        trivy fs --severity CRITICAL --output trivy_report_frontend.txt ./weather-ui
                        '''

                        def backendCritical = sh(script: "grep -o 'CRITICAL:' trivy_report_backend.txt | wc -l", returnStdout: true).trim().toInteger()
                        def frontendCritical = sh(script: "grep -o 'CRITICAL:' trivy_report_frontend.txt | wc -l", returnStdout: true).trim().toInteger()

                        echo "Critical vulnerabilities - Backend: ${backendCritical}, Frontend: ${frontendCritical}"

                        if (backendCritical > env.CRITICAL_THRESHOLD.toInteger() || frontendCritical > env.CRITICAL_THRESHOLD.toInteger()) {
                            error "Dependency scan failed: Critical vulnerabilities exceed the threshold (${env.CRITICAL_THRESHOLD})"
                        } else {
                            echo "Dependency scan passed: Critical vulnerabilities are within acceptable limits."
                        }
                    } catch (Exception e) {
                        error "Dependency scan failed: ${e.message}"
                    }
                }
            }
        }


        stage('Static Analysis') {
            when {
                expression { params.RUN_STATIC_ANALYSIS == true }
            }
            steps {
                script {
                    currentStage = "Static Analysis"
                    def scannerHome = tool 'sonarQube scanner'
                    
                    withSonarQubeEnv('sq1') {
                        sh """
                        ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.projectName=${SONAR_PROJECT_NAME} \
                            -Dsonar.sources=. \
                            -Dsonar.python.version=3.10 \
                            -Dsonar.javascript.file.suffixes=.js,.jsx,.ts,.tsx \
                            -Dsonar.python.file.suffixes=.py
                        """
                    }
                    
                    timeout(time: 1, unit: 'MINUTES') {
                        def qualityGate = waitForQualityGate()
                        if (qualityGate.status != 'OK') {
                            error "Quality gate failed: ${qualityGate.status}"
                        }
                    }
                }
            }
        }

        stage('Dockerfile Scan') {
            steps {
                script {
                    currentStage = 'Dockerfile Scan'
                    def trivyResults = sh(
                        script: """
                            trivy config ./backend/dockerfile \
                            --severity HIGH,CRITICAL \
                            --format table \
                            --exit-code 1 \
                            --output trivy-dockerfile-report.json
                        """,
                        returnStatus: true
                    )
                    if (trivyResults !=0) {
                        sh "aws s3 cp s3://${S3REPORTS}/trivy-dockerfile-report.json"
                        error "Dockerfile security vulnerabilites detected!"
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    currentStage = 'Build'
                    sh "aws s3 cp s3://${S3FRONT}/previous.hash . || echo 'No previous.hash file in S3'"

                    sh "find weather-ui -type f | sort | xargs sha1sum > current.hash"

                    def previousHashExists = fileExists('previous.hash')
                    def shouldBuild = false
                    if (previousHashExists) {
                        def currentHash = sh(script: "sha1sum current.hash | cut -d' ' -f1", returnStdout: true).trim()
                        def previousHash = sh(script: "sha1sum previous.hash | cut -d' ' -f1", returnStdout: true).trim()
                        shouldBuild = (currentHash != previousHash)
                    } else {
                        shouldBuild = true 
                    }

                    env.SHOULD_BUILD = shouldBuild.toString()

                    if (shouldBuild) {
                        echo "Changes detected in weather-ui. Running npm build in Docker..."
                        sh 'cd weather-ui && npm install --cache /tmp/.npm'
                        sh 'cd weather-ui && npm run build'
                    } else {
                        echo "No changes detected in weather-ui. Skipping npm build."
                    }

                    sh """
                        cd backend
                        echo 'Tag: ${TAG}'
                        docker build -t backend:${TAG} .
                        cd ..
                    """
                }
            }
}


        stage('Container Image Scanning') {
            when {
              expression { params.RUN_IMAGE_SCAN == true }
            }
            steps {
                script {
                    currentStage = 'Container Image Scanning'
                    def trivyResults = sh(
                        script: """
                            trivy image \
                            --severity HIGH,CRITICAL \
                            --format table \
                            --exit-code 1 \
                            --output triviy-scan-report.json \
                            backend:${env.TAG}
                        """,
                        returnStatus: true
                    )

                    if (trivyResults != 0) {
                        sh "aws s3 cp triviy-scan-report.json s3://${S3REPORTS}/triviy-scan-report.json"
                        error "Docker Image security vulnerabilities detected!"
                    }
                }
            }
        }


        stage('Tests') {
            when {
              expression { params.RUN_TESTS == true }
            }
            steps {
                script {
                    currentStage = 'Tests'
                    try {
                        sh '''
                            curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
                            chmod +x /usr/local/bin/docker-compose
                        '''
                        sh """
                            export BUILD_NUMBER=${env.TAG}
                            envsubst < tests/docker-compose.yaml > tests/docker-compose.tmp.yaml
                            docker-compose -f tests/docker-compose.tmp.yaml up -d
                        """
                    } catch (e) {
                        currentBuild.result = 'FAILURE'
                        error("Tests failed: ${e}")
                    } finally {
                        sh 'docker-compose -f tests/docker-compose.tmp.yaml down || true'
                    }
                }
            }
        }

        stage('Publish') {
            steps {
                script {
                    currentStage = 'Publish'
                    sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
                    echo "Logged in to ECR"

                    sh """
                    docker tag backend:${env.TAG} ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${env.TAG}
                    docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${env.TAG}
                    """
                }
            }
        }

        stage("Container Signing") {
            steps {
                script {
                    currentStage = "Container Signing"
                    withCredentials([
                        file(credentialsId: 'cosign-private-key', variable: 'COSIGN_KEY'),
                        string(credentialsId: 'cosign-password', variable: 'COSIGN_PASSWORD') 
                    ]) {
                        withEnv(["COSIGN_PASSWORD=${COSIGN_PASSWORD}"]) {
                            sh """
                                cosign sign --key ${COSIGN_KEY} --allow-insecure-registry ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${env.TAG}
                            """
                        }
                    }
                }
            }
        }

        stage('Verify Container Image') {
            steps {
                script {
                    currentStage = "Verify Container Image"
                    
                    withCredentials([
                        file(credentialsId: 'cosign-public-key', variable: 'COSIGN_PUBLIC')
                    ]) {
                        def verifyResult = sh(
                            script: """
                            cosign verify \
                            --key $COSIGN_PUBLIC \
                            --allow-insecure-registry \
                            --insecure-ignore-tlog=true \
                            ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${TAG}
                            """,
                            returnStatus: true
                        )
                        
                        if (verifyResult != 0) {
                            error "Image signature verification failed!"
                        } else {
                            echo "Image signature successfully verified."
                        }
                    }
                }
            }
        }


        stage('Deploy') {
            when {
                expression { env.BRANCH_NAME == 'master' }
            }
            steps {
                script {
                    currentStage = 'Deploy'

                    sh "aws eks --region ${AWS_REGION} update-kubeconfig --name ${EKS_CLUSTER_NAME}"

                    def backendDeploymentExists = sh(script: "kubectl get deployment weatherapp-backend", returnStatus: true)
                    if (backendDeploymentExists != 0) {
                        echo "Backend deployment not found, creating a new one..."
                        sh "kubectl apply -f terraform/deploy.yaml"
                        sh "kubectl apply -f terraform/service.yaml"
                    } else {
                        echo "Backend deployment already exists, updating image..."
                        sh "kubectl set image deploy/weatherapp-backend weatherapp-backend=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}:${env.TAG}"
                    }

                    echo "Backend deployed/updated successfully."

                    if (env.SHOULD_BUILD.toBoolean()) {
                        sh "aws s3 sync weather-ui/build/ s3://${S3FRONT}/ --delete"
                        sh "mv current.hash previous.hash"
                        sh "aws s3 cp previous.hash s3://${S3FRONT}/previous.hash"
                    } else {
                        echo "No changes detected in frontend, skipping sync."
                    }
                }
            }
        }
    }

    post {
        failure {
            slackSend channel: 'devops-alerts', color: 'danger', message: "Build #${currentBuild.number} failed in ${currentStage}."
        }
        success {
            slackSend channel: 'devops-alerts', color: 'good', message: "Build #${currentBuild.number} succeeded in ${currentStage}."
        }
        cleanup {
            sh 'docker system prune -af'
            cleanWs()
        }
    }
}
