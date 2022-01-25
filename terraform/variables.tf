# Export: export TF_VAR_sns_signups_arn=...

variable "sns_signups_arn" {
  description = "ARN of signup notifications"
  type        = string
  sensitive   = true
}

variable "lambda_jar_file" {
  default = "../target/signup-notifications-1.0-SNAPSHOT.jar"
}

variable "lambda_signups_function_handler" {
  default = "walaniam.aws.signup.compiling.SignupNotificationsHandler"
}

variable "lambda_notifications_function_handler" {
  default = "walaniam.aws.signup.notifications.WelcomeNotificationsHandler"
}

variable "lambda_runtime" {
  default = "java8"
}

variable "lambda_env_notification_template" {
  description = "Notification message template"
  type        = string
  sensitive   = true
}

variable "lambda_env_notification_sender" {
  description = "Notification message sender email"
  type        = string
  sensitive   = true
}

variable "lambda_env_notification_rest" {
  description = "Notification REST endpoint"
  type        = string
  sensitive   = true
}