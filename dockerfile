# Multi-stage build for WatGuessr Spring Boot application

# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first (for better caching)
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install OpenSSL for JWT key generation
RUN apt-get update && apt-get install -y openssl && rm -rf /var/lib/apt/lists/*

# Create a non-root user to run the application
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Create jwt directory and generate keys
RUN mkdir -p jwt && \
    openssl genpkey -algorithm RSA -out jwt/private-key.pem -pkeyopt rsa_keygen_bits:2048 && \
    openssl rsa -pubout -in jwt/private-key.pem -out jwt/public-key.pem && \
    chmod 600 jwt/private-key.pem && \
    chmod 644 jwt/public-key.pem && \
    chown -R appuser:appuser jwt

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set ownership of the application files to the non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
