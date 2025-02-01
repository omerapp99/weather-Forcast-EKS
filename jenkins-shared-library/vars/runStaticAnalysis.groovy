
// vars/runStaticAnalysis.groovy
def call() {
    def utils = new org.example.BuildUtils(this)
    utils.runSonarAnalysis(env.SONAR_PROJECT_KEY, env.SONAR_PROJECT_NAME)

    timeout(time: 1, unit: 'MINUTES') {
        def qualityGate = waitForQualityGate()
        if (qualityGate.status != 'OK') {
            error "Quality gate failed: ${qualityGate.status}"
        }
    }
}