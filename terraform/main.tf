terraform {
  required_providers {
    cloudflare = {
      source  = "cloudflare/cloudflare"
      version = "~> 4.0"
    }
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = "~> 2.0"
    }
  }
}

provider "cloudflare" {
  api_token = var.cloudflare_api_token
}

provider "digitalocean" {
  token = var.do_token
}


# Create Tunnel
resource "cloudflare_zero_trust_tunnel_cloudflared" "macbook_tunnel" {
    account_id = var.cloudflare_account_id
    name       = "watguessr_local_dev"
    secret     = base64encode("a-random-32-character-string-here") # We will automate this later
}

# Create the DNS record
resource "cloudflare_record" "dev_link" {
    zone_id    = var.cloudflare_zone_id
    name       = "dev" #creates dev.watguessr.io
    content      = "${cloudflare_zero_trust_tunnel_cloudflared.macbook_tunnel.id}.cfargotunnel.com"
    type       = "CNAME"
    proxied    = true
}

# Configure where the traffic goes locally

resource "cloudflare_zero_trust_tunnel_cloudflared_config" "macbook_config" {
    account_id = var.cloudflare_account_id
    tunnel_id  = cloudflare_zero_trust_tunnel_cloudflared.macbook_tunnel.id # check if id is avail

    config {
        ingress_rule {
            hostname = "dev.watguessr.com"
            service  = "http://localhost:8080" # Points to your Spring Boot app
        }
        # catch-all to prevent errors
        ingress_rule {
            service = "http_status:404"
        }
    }
}

data "digitalocean_ssh_key" "main" {
    name = "terraform-watguessr"
}

output "tunnel_token" {
    value     = cloudflare_zero_trust_tunnel_cloudflared.macbook_tunnel.tunnel_token
    sensitive = true
}

