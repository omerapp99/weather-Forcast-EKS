def call() {
    def valueFile = env.BRANCH_NAME == 'master' ? 'values-prod.yaml' : 'values-dev.yaml'

    withCredentials([string(credentialsId: 'GITLAB_PAT', variable: 'GITLAB_TOKEN')]) {
        sh """
            git clone http://gitlab-ci-token:${GITLAB_TOKEN}@${env.HELM_CHARTS_REPO}
            cd weathereks_iac/weatherapp
            sed -i 's|tag: .*|tag: \"${env.TAG}\"|' ${valueFile}
            git config user.email "jenkins@ci.com"
            git config user.name "Jenkins"
            git add ${valueFile}
            git commit -m "Update image tag to ${env.TAG} [skip ci]"
            git push http://gitlab-ci-token:${GITLAB_TOKEN}@${env.HELM_CHARTS_REPO} master
        """
    }
}