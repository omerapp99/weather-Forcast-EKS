// vars/weatherPipeline.groovy
def call(Map config = [:]) {
    pipeline {
        agent {
            kubernetes {
                label 'eks-agent'
            }
        }

        environment {
            SONAR_PROJECT_KEY = "${config.sonarProjectKey ?: 'YOUR_SONAR_PROJECT_KEY'}"
            SONAR_PROJECT_NAME = "${config.sonarProjectName ?: 'YOUR_SONAR_PROJECT_NAME'}"
            FRONTEND_PORT = "${config.frontendPort ?: '3000'}"
            BACKEND_PORT = "${config.backendPort ?: '5000'}"
            ECR_REPOSITORY_NAME = "${config.ecrRepoName ?: 'YOUR_ECR_REPO'}"
            AWS_REGION = "${config.awsRegion ?: 'YOUR_AWS_REGION'}"
            AWS_ACCOUNT_ID = "${config.awsAccountId ?: 'YOUR_AWS_ACCOUNT_ID'}"
            EKS_CLUSTER_NAME = "${config.eksClusterName ?: 'YOUR_EKS_CLUSTER'}"
            S3FRONT = "${config.s3Front ?: 'YOUR_S3_FRONTEND_BUCKET'}"
            S3REPORTS = "${config.s3Reports ?: 'YOUR_S3_REPORTS_BUCKET'}"
            CRITICAL_THRESHOLD = "${config.criticalThreshold ?: '0'}"
            TAG = "${env.BRANCH_NAME == 'master' ? env.BUILD_TAG : env.GIT_COMMIT}"
            HELM_CHARTS_REPO = "${config.helmChartsRepo ?: 'YOUR_HELM_CHARTS_REPO'}"
            SKIP_CI = 'false'
        }

        parameters {
            booleanParam(name: 'RUN_STATIC_ANALYSIS', defaultValue: false, description: 'Run SonarQube static analysis')
            booleanParam(name: 'RUN_TESTS', defaultValue: false, description: 'Run Tests')
            booleanParam(name: 'RUN_IMAGE_SCAN', defaultValue: false, description: 'Run Image scan')
            booleanParam(name: 'NO_RUN', defaultValue: false, description: 'default no')
        }

        stages {
            stage('Check Build Trigger') {
                steps { script { checkBuildTrigger() } }
            }

            stage('Check Repository Changes') {
                when { expression { env.SKIP_CI == 'false' } }
                steps { script { checkRepositoryChanges() } }
            }

            stage('SBOM Scan') {
                when { expression { params.NO_RUN == true && env.SKIP_CI == 'false' } }
                steps { script { sbomScan() } }
            }

            stage('Static Analysis') {
                when { expression { params.RUN_STATIC_ANALYSIS == true && env.SKIP_CI == 'false' } }
                steps { script { runStaticAnalysis() } }
            }

            stage('Dockerfile Scan') {
                when { expression { params.NO_RUN == true && env.SKIP_CI == 'false' } }
                steps { script { dockerfileScan() } }
            }

            stage('Build') {
                when { expression { env.SKIP_CI == 'false' } }
                steps { script { buildApplication() } }
            }

            stage('Container Image Scanning') {
                when { expression { params.RUN_IMAGE_SCAN == true && env.SKIP_CI == 'false' } }
                steps { script { containerImageScan() } }
            }

            stage('Tests') {
                when { expression { params.RUN_TESTS == true && env.SKIP_CI == 'false' } }
                steps { script { runTests() } }
            }

            stage('Publish') {
                when { expression { env.SKIP_CI == 'false' } }
                steps { script { publishImage() } }
            }

            stage('Container Signing') {
                when { expression { params.NO_RUN == true && env.SKIP_CI == 'false' } }
                steps { script { signContainer() } }
            }

            stage('Verify Container Image') {
                when { expression { params.NO_RUN == true && env.SKIP_CI == 'false' } }
                steps { script { verifyContainerImage() } }
            }

            stage('Update Helm Charts') {
                when { expression { env.SKIP_CI == 'false' } }
                steps { script { updateHelmCharts() } }
            }

            stage('Deploy Frontend') {
                when { expression { env.SKIP_CI == 'false' && env.SKIP_BUILD == 'true'} }
                steps { script { deployFrontend() } }
            }
        }

        post {
            failure { slackNotify('danger', "Build #${currentBuild.number} failed in ${env.STAGE_NAME ?: 'Unknown Stage'}.") }
            success { slackNotify('good', "Build #${currentBuild.number} succeeded in ${env.STAGE_NAME ?: 'Unknown Stage'}.") }
            cleanup { script { cleanupWorkspace() } }
        }
    }
}