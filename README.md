# aws-signup-notifications

# Prerequisites
Below instructions were tested on [WSL2 - Ubuntu](https://ubuntu.com/wsl)
## Install Terraform
Use Ubuntu instructions from https://www.terraform.io/downloads
```bash
curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add -
sudo apt-add-repository "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main"
sudo apt-get update && sudo apt-get install terraform
```

Verify installation. You shoud see something like:
```bash
terraform -v
Terraform v1.1.4
on linux_amd64
```

## AWS CLI
### Install
Install [AWS CLI](https://aws.amazon.com/cli/)
```bash
sudo apt install awscli
```

Verify installation
```bash
aws --version
```
### Configure
```bash
aws configure
```

# AWS Resources
https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/sqs_queue  
https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/sns_topic_subscription
