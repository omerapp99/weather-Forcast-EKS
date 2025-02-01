// vars/deployFrontend.groovy
def call() {
    def utils = new org.example.BuildUtils(this)
    utils.deployToS3(env.S3FRONT)
}