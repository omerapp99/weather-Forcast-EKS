// vars/checkBuildTrigger.groovy
def call() {
    sh """
        git config --global --add safe.directory /var/lib/jenkins/workspace/EKSWeather_master
    """
    def lastCommitMsg = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
    if (lastCommitMsg.contains('[skip ci]')) {
        env.SKIP_CI = 'true'
        currentBuild.result = 'SUCCESS'
        return
    }
}
