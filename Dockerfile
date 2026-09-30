FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy project files
COPY pom.xml .
COPY src ./src

# Install Playwright and build
RUN mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium" || true
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install required libraries for Playwright
RUN apt-get update && apt-get install -y \
    libssl3 \
    libgconf-2-4 \
    libnss3 \
    libxss1 \
    libasound2 \
    libgtk-3-0 \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Copy built JAR from builder
COPY --from=builder /build/target/playstation-stock-monitor-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
