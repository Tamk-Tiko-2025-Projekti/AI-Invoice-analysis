FROM node:21 AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Install Python and other tools
RUN apk add --no-cache python3 py3-pip bash curl \
    poppler poppler-utils

# Copy backend code first
COPY spring_back/ ./spring_back/

# Create Python virtual environment in the expected location
WORKDIR /app/spring_back/App
RUN python3 -m venv venv && \
    . ./venv/bin/activate && \
    pip install --no-cache-dir --upgrade pip setuptools wheel && \
    pip install --no-cache-dir -r requirements.txt

# Install Gradle
ENV GRADLE_VERSION=8.12
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    rm /tmp/gradle-${GRADLE_VERSION}-bin.zip
ENV PATH="${PATH}:/opt/gradle/gradle-${GRADLE_VERSION}/bin"

# Copy frontend build to Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist/ ./src/main/resources/static/

# Build the application
RUN gradle build -x test --no-daemon

# Expose port for the application
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]
