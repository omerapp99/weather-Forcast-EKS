// vars/buildApplication.groovy
def call() {
    def utils = new org.example.BuildUtils(this)
    
    if (env.SKIP_BUILD == 'true') {
        echo "Skipping build stage as SKIP_BUILD is true."
    } else {
        utils.buildFrontend()
    }
    utils.buildBackend(env.TAG)
}