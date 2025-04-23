# Stage 1: Build React frontend
FROM node:21-alpine as frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install --omit=dev
COPY frontend/ ./
RUN npm run build

# Stage 2: Main application with JDK, Gradle, and Python
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Install Python and required tools
RUN apk add --no-cache python3 py3-pip && \
    ln -sf python3 /usr/bin/python && \
    python -m ensurepip && \
    pip install --upgrade pip && \
    apk add --no-cache bash curl

# Install Gradle
ENV GRADLE_VERSION=8.12
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    rm /tmp/gradle-${GRADLE_VERSION}-bin.zip
ENV PATH="${PATH}:/opt/gradle/gradle-${GRADLE_VERSION}/bin"

# Copy Python requirements and install dependencies
COPY spring_back/App/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copy backend code
COPY spring_back/ ./spring_back/
COPY dev_prompt.txt user_prompt.txt turn_to_json.txt ./

# Copy frontend build to Spring Boot static resources
COPY --from=frontend-build /app/frontend/dist/ ./spring_back/App/src/main/resources/static/

# Set working directory for the Spring Boot app
WORKDIR /app/spring_back/App

# Build the application
RUN gradle build -x test --no-daemon

# Expose port for the application
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "build/libs/App-0.0.1-SNAPSHOT.jar"]
