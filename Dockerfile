# Stage 1: Build the application using Maven
FROM maven:3.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Build the "fat JAR"
RUN mvn package

# Stage 2: Create the final runtime image
FROM debian:bullseye-slim
# Install Java and the libraries needed for a GUI app to run
RUN apt-get update && apt-get install -y \
    openjdk-17-jre-headless \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*

# Set up a non-root user for better security
RUN groupadd -r appuser && useradd -r -g appuser -d /home/appuser -s /bin/bash appuser
RUN mkdir /app && chown appuser:appuser /app
USER appuser
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /app/target/image-annotator-1.0.0.jar ./app.jar

# The command to run the Swing application
CMD ["java", "-jar", "/app/app.jar"]