// vars/slackNotify.groovy
def call(String color, String message) {
    slackSend channel: 'devops-alerts', color: color, message: message
}