// vars/cleanupWorkspace.groovy
def call() {
    try {
        cleanWs()
    } catch (Exception e) {
        echo "Failed to clean up workspace: ${e.message}"
    }
}