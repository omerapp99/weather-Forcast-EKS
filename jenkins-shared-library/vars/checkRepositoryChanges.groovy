// vars/checkRepositoryChanges.groovy
def call() {
    def currentHash = sh(script: 'find weather-ui -type f | sort | xargs sha1sum', returnStdout: true).trim()
    def hashFile = 'weather-ui.hash'

    if (fileExists(hashFile)) {
        def previousHash = readFile(hashFile).trim()
        if (currentHash == previousHash) {
            env.SKIP_BUILD = 'true'
        } else {
            writeFile file: hashFile, text: currentHash
            env.SKIP_BUILD = 'false'
        }
    } else {
        writeFile file: hashFile, text: currentHash
        env.SKIP_BUILD = 'false'
    }
}