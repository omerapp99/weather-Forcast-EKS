def call() {
    try {
        sh """
            export BUILD_NUMBER=${env.TAG}
            envsubst < tests/docker-compose.yaml > tests/podman-compose.yaml
            podman-compose -f tests/podman-compose.yaml up -d
        """
    } catch (e) {
        currentBuild.result = 'FAILURE'
        error("Tests failed: ${e}")
    } finally {
        sh 'podman-compose -f tests/podman-compose.yaml down || true'
    }
}