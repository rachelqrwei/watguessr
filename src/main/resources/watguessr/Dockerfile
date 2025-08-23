# Multi-stage build for Vue.js frontend

# Build stage
FROM node:20-alpine AS build

# Set build-time environment variables for Vite
ARG VITE_MAPBOX_TOKEN
ARG VITE_API_BASE_URL
ARG VITE_GOOGLE_CLIENT_ID
ARG VITE_GOOGLE_CLIENT_SECRET
ARG VITE_GOOGLE_REDIRECT_URI

# Create a non-root user for the build stage
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# Set working directory and change ownership
WORKDIR /app
RUN chown -R appuser:appgroup /app

# Switch to non-root user for build
USER appuser

# Copy package files with correct permissions
COPY --chown=appuser:appgroup package*.json ./

# Install dependencies
RUN npm ci

# Copy source code with correct permissions
COPY --chown=appuser:appgroup . .

# Build the application with environment variables
RUN npm run build

# Production stage - serve with nginx
FROM nginx:alpine

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Create custom nginx configuration with proper SSL and IPv4 settings
COPY <<EOF /etc/nginx/conf.d/default.conf
resolver 8.8.8.8 ipv6=off;

server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;
    
    # Handle Vue Router - serve index.html for all frontend routes
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # Proxy API requests to your backend
    location /api/ {
        proxy_pass https://watguessr-j5xct.ondigitalocean.app/api/;
        proxy_ssl_server_name on;
        proxy_ssl_verify off;
        proxy_set_header Host watguessr-j5xct.ondigitalocean.app;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_buffering off;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Proxy WebSocket connections
    location /ws-matchmaking/ {
        proxy_pass https://watguessr-j5xct.ondigitalocean.app/ws-matchmaking/;
        proxy_ssl_server_name on;
        proxy_ssl_verify off;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host watguessr-j5xct.ondigitalocean.app;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# Copy built files from build stage
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]