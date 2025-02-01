// vars/sbomScan.groovy
def call() {
    try {
        sh '''
        trivy fs --severity CRITICAL --output trivy_report_backend.txt ./backend
        trivy fs --severity CRITICAL --output trivy_report_frontend.txt ./weather-ui
        '''

        def backendCritical = sh(script: "grep -o 'CRITICAL:' trivy_report_backend.txt | wc -l", returnStdout: true).trim().toInteger()
        def frontendCritical = sh(script: "grep -o 'CRITICAL:' trivy_report_frontend.txt | wc -l", returnStdout: true).trim().toInteger()

        if (backendCritical > env.CRITICAL_THRESHOLD.toInteger() || frontendCritical > env.CRITICAL_THRESHOLD.toInteger()) {
            error "Dependency scan failed: Critical vulnerabilities exceed the threshold (${env.CRITICAL_THRESHOLD})"
        }
    } catch (Exception e) {
        error "Dependency scan failed: ${e.message}"
    }
}
