def call() {
    withCredentials([
        file(credentialsId: 'cosign-public-key', variable: 'COSIGN_PUBLIC')
    ]) {
        def verifyResult = sh(
            script: """
            cosign verify \
            --key $COSIGN_PUBLIC \
            --allow-insecure-registry \
            --insecure-ignore-tlog=true \
            ${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.ECR_REPOSITORY_NAME}:${env.TAG}
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