def call() {
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
    if (trivyResults != 0) {
        sh "aws s3 cp trivy-dockerfile-report.json s3://${env.S3REPORTS}/trivy-dockerfile-report.json"
        error "Dockerfile security vulnerabilities detected!"
    }
}