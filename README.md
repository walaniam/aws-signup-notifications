# aws-signup-notifications

# Prerequisites
## Install Terraform on WSL2
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
