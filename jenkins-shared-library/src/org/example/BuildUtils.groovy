// src/org/example/BuildUtils.groovy
package org.example

class BuildUtils implements Serializable {
    def script

    BuildUtils(script) {
        this.script = script
    }

    def runSonarAnalysis(String projectKey, String projectName) {
        def scannerHome = script.tool 'sonarQube scanner'
        script.withSonarQubeEnv('sq1') {
            script.sh """
                ${scannerHome}/bin/sonar-scanner \
                -Dsonar.projectKey=${projectKey} \
                -Dsonar.projectName=${projectName} \
                -Dsonar.sources=. \
                -Dsonar.python.version=3.10 \
                -Dsonar.javascript.file.suffixes=.js,.jsx,.ts,.tsx \
                -Dsonar.python.file.suffixes=.py
            """
        }
    }

    def buildFrontend() {
        script.sh '''
            cd weather-ui
            npm install --cache /tmp/.npm
            npm run build
        '''
    }

    def buildBackend(String tag) {
        script.sh """
            cd backend
            podman build -t backend:${tag} .
            cd ..
        """
    }

    def publishToECR(String tag, String accountId, String region, String repository) {
        script.sh """
            aws ecr get-login-password --region ${region} | podman login --username AWS --password-stdin ${accountId}.dkr.ecr.${region}.amazonaws.com
            podman tag backend:${tag} ${accountId}.dkr.ecr.${region}.amazonaws.com/${repository}:${tag}
            podman push ${accountId}.dkr.ecr.${region}.amazonaws.com/${repository}:${tag}
        """
    }

    def deployToS3(String bucketName) {
        script.sh "aws s3 sync weather-ui/build/ s3://${bucketName}/ --delete"
    }
}