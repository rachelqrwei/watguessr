# Multi-stage build for Vue.js frontend - Static hosting version

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

# Production stage - serve static files
FROM nginx:alpine

# Remove default nginx config and create a simple static file server
RUN rm /etc/nginx/conf.d/default.conf

# Create nginx configuration for SPA routing
COPY <<EOF /etc/nginx/conf.d/default.conf
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Content Security Policy for Google OAuth
    add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://api.mapbox.com https://accounts.google.com https://www.gstatic.com https://www.googleapis.com; style-src 'self' 'unsafe-inline' https://api.mapbox.com https://fonts.googleapis.com; img-src 'self' data: https:; font-src 'self' https://fonts.gstatic.com; connect-src 'self' https://api.mapbox.com https://accounts.google.com https://www.googleapis.com https://oauth2.googleapis.com; frame-src 'self' https://accounts.google.com; base-uri 'self';" always;
    
    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        add_header Access-Control-Allow-Origin "*";
    }
    
    # Handle all routes - serve index.html for any request that doesn't match a file
    location / {
        try_files \$uri \$uri/ /index.html;
        
        # Add headers for HTML files
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
    }
    
    # Explicitly handle 404 errors by serving index.html
    error_page 404 /index.html;
    
    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF

# Copy built files from build stage
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]