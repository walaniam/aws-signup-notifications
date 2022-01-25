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
  topic_arn            = var.sns_signups_arn
  protocol             = "sqs"
  endpoint             = aws_sqs_queue.signups_queue.arn
  raw_message_delivery = true
}