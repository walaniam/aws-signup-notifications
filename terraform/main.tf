terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.72"
    }
  }
}

provider "aws" {
  profile = "default"
  region  = "eu-west-1"
}

resource "aws_sqs_queue" "signups_queue" {
  name                       = "signups_queue"
  message_retention_seconds  = 345600
  delay_seconds              = 300
  visibility_timeout_seconds = 45
}

resource "aws_sns_topic_subscription" "signups_sqs_target" {
  topic_arn = var.sns_signups_arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.signups_queue.arn
}