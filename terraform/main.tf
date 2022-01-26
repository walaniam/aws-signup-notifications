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
