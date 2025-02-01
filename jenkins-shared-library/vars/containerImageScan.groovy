// vars/containerImageScan.groovy
def call() {
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
        sh "aws s3 cp triviy-scan-report.json s3://${env.S3REPORTS}/triviy-scan-report.json"
        error "Container Image security vulnerabilities detected!"
    }
}
