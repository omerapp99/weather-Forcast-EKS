// vars/publishImage.groovy
def call() {
    def utils = new org.example.BuildUtils(this)
    utils.publishToECR(env.TAG, env.AWS_ACCOUNT_ID, env.AWS_REGION, env.ECR_REPOSITORY_NAME)
}