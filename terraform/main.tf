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

###################################################
# DynamoDb
resource "aws_dynamodb_table" "signups" {
  name         = "Signups"
  billing_mode = "PAY_PER_REQUEST"
  ttl {
    attribute_name = "ttl"
    enabled        = true
  }
  hash_key  = "year_month_created"
  range_key = "created_at"
  attribute {
    name = "year_month_created"
    type = "S"
  }
  attribute {
    name = "created_at"
    type = "N"
  }
}

############################################

# SQS queue for welcome notification events
resource "aws_sqs_queue" "welcome_notifications_queue" {
  name                       = "welcome_notifications_queue.fifo"
  message_retention_seconds  = 43200
  delay_seconds              = 300
  visibility_timeout_seconds = 30
  fifo_queue                 = true
}

resource "aws_sqs_queue_policy" "welcome_notifications_queue_policy" {
  queue_url = aws_sqs_queue.welcome_notifications_queue.id
  policy    = <<EOF
{
  "Version": "2012-10-17",
  "Id": "sqspolicy",
  "Statement": [
    {
      "Sid": "AllowLambda",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "sqs:SendMessage",
      "Resource": "${aws_sqs_queue.welcome_notifications_queue.arn}",
      "Condition": {
        "ArnEquals": {
          "aws:SourceArn": "${aws_lambda_function.signups_processing_function.arn}"
        }
      }
    }
  ]
}
EOF
}