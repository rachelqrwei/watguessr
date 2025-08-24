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

# Create simple nginx configuration for static files only
COPY <<EOF /etc/nginx/conf.d/default.conf
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;
    
    # Handle Vue Router - serve index.html for all frontend routes
    location / {
        try_files \$uri \$uri/ /index.html;
    }
    
    # Add CORS headers for any remaining static assets
    add_header 'Access-Control-Allow-Origin' '*' always;
    add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
    add_header 'Access-Control-Allow-Headers' '*' always;
}
EOF

# Copy built files from build stage
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]