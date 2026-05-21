variable "cloudflare_api_token" {
  type      = string
  sensitive = true
}

variable "cloudflare_zone_id" {
  type = string
}

variable "cloudflare_account_id" {
  type = string
}

variable "do_token"  {
  type      = string
  sensitive = true
}

variable "tunnel_secret" {
    type      = string
    sensitive = true
}

variable "do_ssh_key_id" {
    type = string
}
