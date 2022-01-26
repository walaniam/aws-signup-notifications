# Lambda function
resource "aws_lambda_function" "signups_processing_function" {
  function_name    = "signups_processing_function"
  filename         = var.lambda_jar_file
  source_code_hash = base64sha256(filebase64(var.lambda_jar_file))
  handler          = var.lambda_signups_function_handler
  runtime          = var.lambda_runtime
  role             = aws_iam_role.lambda_role.arn
  memory_size      = 512
  timeout          = 30
  environment {
    variables = {
      NOTIFICATION_JOINED_ORGANIZATION = var.lambda_env_notification_joined_organization
      NOTIFICATION_SENDER              = var.lambda_env_notification_sender
      NOTIFICATION_TARGET_QUEUE        = aws_sqs_queue.welcome_notifications_queue.id
    }
  }
}

# Lambda trigger
resource "aws_lambda_event_source_mapping" "sqs_to_lambda" {
  event_source_arn                   = aws_sqs_queue.signups_queue.arn
  function_name                      = aws_lambda_function.signups_processing_function.arn
  batch_size                         = 5
  maximum_batching_window_in_seconds = 300
  enabled                            = true
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
              "sqs:SendMessage",
              "sqs:TagQueue",
              "sqs:UntagQueue"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy" "lambda_role_dynamodb_policy" {
  name   = "AllowDynamoDbPermissions"
  role   = aws_iam_role.lambda_role.id
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "ReadWriteTable",
            "Effect": "Allow",
            "Action": [
                "dynamodb:BatchGetItem",
                "dynamodb:GetItem",
                "dynamodb:Query",
                "dynamodb:Scan",
                "dynamodb:BatchWriteItem",
                "dynamodb:PutItem",
                "dynamodb:UpdateItem"
            ],
            "Resource": "${aws_dynamodb_table.signups.arn}"
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