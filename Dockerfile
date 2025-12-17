# Multi-stage build for optimized image size

# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent pom and module poms first to leverage Docker cache
COPY pom.xml .
COPY signature-core/pom.xml signature-core/
COPY signature-spring-boot/pom.xml signature-spring-boot/
COPY signature-app/pom.xml signature-app/

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B || true

# Copy source code for all modules
COPY signature-core/src signature-core/src
COPY signature-spring-boot/src signature-spring-boot/src
COPY signature-app/src signature-app/src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -g 1001 appuser && \
    useradd -r -u 1001 -g appuser appuser

WORKDIR /app

# Copy the built JAR from builder stage (signature-app module)
COPY --from=builder /app/signature-app/target/signature-app-*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
