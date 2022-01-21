# Export: export TF_VAR_sns_signups_arn=...

variable "sns_signups_arn" {
  description = "ARN of signup notifications"
  type        = string
  sensitive   = true
}

variable "lambda_jar_file" {
  default = "../target/signup-notifications-1.0-SNAPSHOT.jar"
}

variable "lambda_function_handler" {
  default = "walaniam.aws.signup.SignupNotificationsHandler"
}

variable "lambda_runtime" {
  default = "java8"
}
