# Provider Configuration for AWS
provider "aws" {
  region = "eu-north-1"
}

# Provider Configuration for Kubernetes
provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  token                  = data.aws_eks_cluster_auth.main.token
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
}

# Data Source for EKS Cluster Authentication
data "aws_eks_cluster_auth" "main" {
  name = module.eks.cluster_name
}

# Data Source for Existing VPC
data "aws_vpc" "existing_vpc" {
  id = "vpc-0595f064809ac0b06"
}

# EKS Cluster Module
module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  cluster_name    = "eks-cluster"
  cluster_version = "1.31"
  vpc_id          = "vpc-0595f064809ac0b06"
  subnet_ids      = ["subnet-095e8ea035eb47a9e", "subnet-0818b7a5c4dd8895a"]

  # Enable public and private endpoints
  cluster_endpoint_private_access      = true
  cluster_endpoint_public_access       = true
  cluster_endpoint_public_access_cidrs = ["0.0.0.0/0"]
}

# IAM Role for EKS Node Group
resource "aws_iam_role" "eks_node_role" {
  name = "eks-node-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

# Attach Policies to the EKS Node Role
resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

resource "aws_iam_role_policy_attachment" "ec2_container_registry_read_only" {
  role       = aws_iam_role.eks_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# Node Group Resource for EKS
resource "aws_eks_node_group" "example" {
  cluster_name    = module.eks.cluster_name
  node_group_name = "eks-cluster-node-group"
  node_role_arn   = aws_iam_role.eks_node_role.arn
  subnet_ids      = ["subnet-095e8ea035eb47a9e", "subnet-0818b7a5c4dd8895a"]

  scaling_config {
    desired_size = 2
    max_size     = 3
    min_size     = 1
  }

  instance_types = ["t3.micro"]
  ami_type       = "AL2_x86_64"
}

# Add aws-auth ConfigMap to the Cluster
resource "kubernetes_config_map" "aws_auth" {
  metadata {
    name      = "aws-auth"
    namespace = "kube-system"
  }

  data = {
    mapRoles = yamlencode([
      {
        rolearn  = "arn:aws:iam::205337945058:role/jenkins-ec2-role"
        username = "jenkins"
        groups   = ["system:masters"]
      }
    ])
  }

  depends_on = [module.eks]
}

# Security Group for the EKS Cluster
resource "aws_security_group" "eks_cluster_sg" {
  vpc_id = data.aws_vpc.existing_vpc.id

  # Allow traffic from JenkinsVPC (sg-0ce5ea1ebb47542df)
  ingress {
    from_port       = 0
    to_port         = 65535
    protocol        = "tcp"
    security_groups = ["sg-0ce5ea1ebb47542df"]
    description     = "Allow Jenkins"
  }

  # Allow traffic on port 5000 from anywhere (0.0.0.0/0)
  ingress {
    from_port   = 5000
    to_port     = 5000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow everyone on port 5000"
  }

  # Allow outbound traffic (default)
  egress {
    from_port   = 0
    to_port     = 65535
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
