// vars/signContainer.groovy
def call() {
    withCredentials([
        file(credentialsId: 'cosign-private-key', variable: 'COSIGN_KEY')
    ]) {
        withEnv(["COSIGN_PASSWORD=1234"]) {
            sh """
                cosign sign --key ${COSIGN_KEY} --allow-insecure-registry ${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.ECR_REPOSITORY_NAME}:${env.TAG}
            """
        }
    }
}