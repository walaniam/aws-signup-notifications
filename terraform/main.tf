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

# SQS queue for signup events
resource "aws_sqs_queue" "signups_queue" {
  name                       = "signups_queue"
  message_retention_seconds  = 43200
  delay_seconds              = 300
  visibility_timeout_seconds = 30
}

resource "aws_sqs_queue_policy" "signups_queue_policy" {
  queue_url = aws_sqs_queue.signups_queue.id
  policy    = <<EOF
{
  "Version": "2012-10-17",
  "Id": "sqspolicy",
  "Statement": [
    {
      "Sid": "First",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "sqs:SendMessage",
      "Resource": "${aws_sqs_queue.signups_queue.arn}",
      "Condition": {
        "ArnEquals": {
          "aws:SourceArn": "${var.sns_signups_arn}"
        }
      }
    }
  ]
}
EOF
}

# Subscribe SQS queue to SNS topic
resource "aws_sns_topic_subscription" "signups_sqs_target" {
  topic_arn = var.sns_signups_arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.signups_queue.arn
}

# Lambda function
resource "aws_lambda_function" "signups_processing_function" {
  function_name    = "signups_processing_function"
  filename         = var.lambda_jar_file
  source_code_hash = base64sha256(filebase64(var.lambda_jar_file))
  handler          = var.lambda_function_handler
  runtime          = var.lambda_runtime
  role             = aws_iam_role.lambda_role.arn
  memory_size      = 196
}

# Lambda trigger
resource "aws_lambda_event_source_mapping" "sqs_to_lambda" {
  event_source_arn = aws_sqs_queue.signups_queue.arn
  function_name    = aws_lambda_function.signups_processing_function.arn
  enabled          = false
}

# Lambda role
resource "aws_iam_role" "lambda_role" {
  name               = "LambdaRole"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
        "Action": "sts:AssumeRole",
        "Effect": "Allow",
        "Principal": {
            "Service": "lambda.amazonaws.com"
        }
    }
  ]
}
EOF
}

resource "aws_iam_role_policy" "lambda_role_sqs_policy" {
  name   = "AllowSQSPermissions"
  role   = aws_iam_role.lambda_role.id
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
              "sqs:GetQueueAttributes",
              "sqs:DeleteMessage",
              "sqs:ChangeMessageVisibility",
              "sqs:ReceiveMessage",
              "sqs:TagQueue",
              "sqs:UntagQueue"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy" "lambda_role_logs_policy" {
  name   = "AllowLogging"
  role   = aws_iam_role.lambda_role.id
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Effect": "Allow",
      "Resource": "*"
    }
  ]
}
EOF
}
