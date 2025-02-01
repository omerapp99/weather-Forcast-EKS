@Library('weather-pipeline-lib') _

weatherPipeline([
    vaultUrl: "YOUR_VAULT_URL",
    vaultCredentialId: "YOUR_VAULT_CREDENTIAL_ID",
    vaultSecretPaths: [
        [
            path: "secret/jenkins",
            vars: [
                [envVar: "awsAccountId", vaultKey: "aws_account_id"],
                [envVar: "sonarToken", vaultKey: "sonar_token"],
                [envVar: "dockerhubUser", vaultKey: "dockerhub_username"],
                [envVar: "dockerhubPassword", vaultKey: "dockerhub_password"],
                [envVar: "githubToken", vaultKey: "github_token"],
                [envVar: "slackToken", vaultKey: "slack_token"],
                [envVar: "awsAccessKey", vaultKey: "aws_access_key"],
                [envVar: "awsSecretKey", vaultKey: "aws_secret_key"],
                [envVar: "sonarProjectKey", vaultKey: "sonar_project_key"],
                [envVar: "sonarProjectName", vaultKey: "sonar_project_name"],
                [envVar: "ecrRepoName", vaultKey: "ecr_repo_name"],
                [envVar: "awsRegion", vaultKey: "aws_region"],
                [envVar: "eksClusterName", vaultKey: "eks_cluster_name"],
                [envVar: "s3Front", vaultKey: "s3_front_bucket"],
                [envVar: "s3Reports", vaultKey: "s3_reports_bucket"],
                [envVar: "helmChartsRepo", vaultKey: "helm_charts_repo"]
            ]
        ]
    ]
])