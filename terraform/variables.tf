# Export: export TF_VAR_sns_signups_arn=...

variable "sns_signups_arn" {
  description = "ARN of signup notifications"
  type        = string
  sensitive   = true
}
