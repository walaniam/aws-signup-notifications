# SQS queue for welcome notification events
resource "aws_sqs_queue" "welcome_notifications_queue" {
  name                       = "welcome_notifications_queue.fifo"
  message_retention_seconds  = 43200
  delay_seconds              = 10
  visibility_timeout_seconds = 180
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