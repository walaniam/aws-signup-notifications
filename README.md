# aws-signup-notifications

Listen to SNS topic about signup events to organization. Send this event to REST endpoint together
with information about other users that joined recently.

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

# Solution Architecture
Lambda functions are implemented in Java 8. Whole infrastructure and Lambda functions deployment is implemented
with Terraform.

Below are main AWS resources used in this solution, listed in order of data flow.

AWS Resource | Name | Purpose
--- | --- | --- |
SQS queue | signups_queue | This queue is subscribed to SNS topic. In this way all notifications are put on the queue and no events are lost. |
Lambda (Java) | signups_processing_function | Triggered by events from *signups_queue*. In 5 element batches. |
DynamoDB table | Signups | Keeps signup events. Used to collect recent joiners in case *signup_processing_function* gets a batch of less than 4 records. |
SQS queue | welcome_notifications_queue.fifo | Gets welcome notification events, deduplicated by notification receiver id. FIFO queue to enforce exactly-once processing |
Lambda (Java) | notifications_sender_function | Triggered by events from *welcome_notifications_queue.fifo* in batch of 1 record. Sends notifications to REST endpoints. Tries 3 times in case of non 2xx response, then execution fails and new event is triggered again. 

# Deployment
## Build Lambda Functions
In project root directory run
```shell
mvn clean install
```

## Terraform deployment
### Initialize Terraform
In project root directory run
```shell
cd terraform
terraform init
```

### Export secret variables
```shell
export TF_VAR_sns_signups_arn=<ARN of SNS topic with signup events>
export TF_VAR_lambda_env_notification_joined_organization=<Name of organization/site/etc which users joined>
export TF_VAR_lambda_env_notification_sender=<Email address of notifications sender>
export TF_VAR_lambda_env_notification_rest=<REST endpoint for posting notification events>
```

### Deploy infrastructure
To see planned changes run
```shell
terraform plan
```

To execute actual deployment run
```shell
terraform apply
```