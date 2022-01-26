# Lambda function
resource "aws_lambda_function" "notifications_sender_function" {
  function_name    = "notifications_sender_function"
  filename         = var.lambda_jar_file
  source_code_hash = base64sha256(filebase64(var.lambda_jar_file))
  handler          = var.lambda_notifications_function_handler
  runtime          = var.lambda_runtime
  role             = aws_iam_role.notifications_lambda_role.arn
  memory_size      = 512
  timeout          = 30
  environment {
    variables = {
      NOTIFICATION_REST = var.lambda_env_notification_rest
    }
  }
}

# Lambda trigger
resource "aws_lambda_event_source_mapping" "notifications_sqs_to_lambda" {
  event_source_arn = aws_sqs_queue.welcome_notifications_queue.arn
  function_name    = aws_lambda_function.notifications_sender_function.arn
  batch_size       = 1
  enabled          = true
}

# Lambda role
resource "aws_iam_role" "notifications_lambda_role" {
  name               = "NotificationsLambdaRole"
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

resource "aws_iam_role_policy" "notifications_lambda_role_sqs_policy" {
  name   = "AllowSQSPermissions"
  role   = aws_iam_role.notifications_lambda_role.id
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

resource "aws_iam_role_policy" "notifications_lambda_role_logs_policy" {
  name   = "AllowLogging"
  role   = aws_iam_role.notifications_lambda_role.id
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