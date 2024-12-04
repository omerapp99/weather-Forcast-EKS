# Weather App CI/CD Pipeline

## Table of Contents
1. [Project Overview](#-project-overview)
2. [Features](#-features)
3. [Prerequisites](#-prerequisites)
4. [Pipeline Workflow](#-pipeline-workflow)
5. [Technologies Used](#-technologies-used)
6. [How to Run](#-how-to-run)
7. [Jenkinsfile Explanation](#-jenkinsfile-explanation)

---

## 📌 Project Overview

This project implements an automated CI/CD pipeline for a Python-based **Weather Forcast Website**, which includes a React frontend and a Gunicorn backend. 
The solution supports seamless integration, testing, version management, and deployment processes:
- The **frontend** is deployed as a static website on **Amazon S3**.
- The **backend** is containerized and deployed to **Amazon EKS**.

Using **Jenkins**, this pipeline ensures continuous integration, static analysis, dependency scanning, testing, containerization, signing, and deployment. **Slack notifications** and **SonarQube** help maintain quality and provide real-time insights.

---

## ✨ Features

- **Dependency and Security Scans**:
  - Scans the backend and frontend for critical vulnerabilities using **Trivy**.
  - Scans Dockerfiles for configuration issues and vulnerabilities.
- **Continuous Integration**:
  - Automatically builds and tests code changes.
  - Runs **SonarQube** static code analysis to ensure code quality.
- **Artifact Management**:
  - Stores backend Docker images in **Amazon ECR**.
- **Container Signing**:
  - Signs backend Docker images using **Cosign** for secure deployments.
- **Version Control**:
  - Automatically tags Docker images based on the branch and build context.
- **Deployment**:
  - Deploys the frontend to **Amazon S3** and the backend to **Amazon EKS**.
- **Notifications**:
  - Sends pipeline status updates (success/failure) to Slack.
- **Environment-Specific Deployments**:
  - **Staging**: For non-master branches.
  - **Production**: For the master branch.

---

## 📋 Prerequisites

- **Jenkins** installed and configured with:
  - Docker Pipeline
  - Amazon Elastic Container Service
  - SSH Agent
  - Slack Notification
- **AWS CLI** installed and configured with necessary permissions.
- **AWS ECR** for Docker image storage.
- **Amazon S3** for frontend hosting and vulnerability scans outputs.
- **Amazon EKS** cluster configured for backend deployment.
- **Terraform** for provisioning infrastructure.
- **SonarQube** for static code analysis.
- **Slack workspace** for notifications.

---

## 🛠️ Pipeline Workflow

1. **Code Checkout**: Pulls the latest code from the Git repository.
2. **Dependency Scan**:
   - Uses **Trivy** to scan backend and frontend for critical vulnerabilities.
3. **Static Analysis**:
   - Scans the code using **SonarQube**.
   - Ensures it meets quality gate requirements.
4. **Dockerfile Scan**:
   - Uses **Trivy** to scan the Dockerfile for vulnerabilities.
5. **Build**:
   - **Frontend**: Detects changes and builds the React project.
   - **Backend**: Builds the Flask Docker image.
5. **Container Scanning**:
   - Uses **Trivy** to scan the backend Docker image for vulnerabilities.
6. **Testing**:
   - Executes frontend and backend tests using **Selenium**.
7. **Container Signing**:
   - Signs backend Docker images with **Cosign**.
8. **Deployment**:
   - Deploys the backend to **EKS**.
   - Synchronizes the frontend build to **S3**.
9. **Post-Build Actions**:
   - Sends Slack notifications for success or failure.
   - Cleans up resources and workspace.

---

## ⚙️ Technologies Used

- **Jenkins**: CI/CD orchestration.
- **Docker**: Containerization.
- **SonarQube**: Static code analysis.
- **Trivy**: Dependency and container scanning.
- **Cosign**: Docker image signing.
- **AWS S3**: Frontend hosting and vulnerability scans outputs .
- **AWS ECR**: Docker image repository.
- **AWS EKS**: Backend container orchestration.
- **Slack**: Notifications.
- **Terraform**: Infrastructure as code.

---

## 🚀 How to Run

### Step 1: Clone the Repository
```bash
git clone https://github.com/omerapp99/weather-Forcast-EKS.git
cd weather-Forcast-EKS
```
### Step 2: Set Up Jenkins
``` 
Install the required plugins:
    Docker Pipeline
    Amazon Elastic Container Service
    SSH Agent
        Slack Notification
Configure Jenkins:
    Assign roles to master and agent nodes.
    Configure Slack integration.
    Define SonarQube scanner and necessary AWS credentials.
```
### Step 3: Provision Infrastructure
```
Use Terraform to create the EKS cluster:
  cd terraform
  terraform init
  terraform apply
```
### Step 4: Trigger the Pipeline
```
Configure Jenkins pipeline parameters:
    Enable/disable specific stages such as Static Analysis or Tests.
Trigger builds manually or configure a webhook for automatic builds.
```
📂 File Structure
```
├── weather-ui/       # React frontend code
├── backend/          # Flask backend code
├── terraform/        # Infrastructure as code (EKS)
├── Jenkinsfile       # CI/CD pipeline script
├── tests/            # Automated tests
├── README.md         # Project documentation
```