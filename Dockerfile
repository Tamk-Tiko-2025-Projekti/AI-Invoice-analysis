FROM node:21 AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21
WORKDIR /app

# Install Python and dependencies
RUN apt-get update && \
    apt-get install -y \
    python3-full \
    python3-venv \
    python3-pip \
    curl \
    unzip \
    poppler-utils \
    libzbar0 \
    libzbar-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy backend code first
COPY spring_back/ ./spring_back/

# Set up Python environment
WORKDIR /app/spring_back/App
RUN python3 -m venv venv --system-site-packages && \
    venv/bin/python3 -m ensurepip --upgrade && \
    venv/bin/python3 -m pip install --upgrade pip setuptools wheel && \
    venv/bin/python3 -m pip install -r requirements.txt

# Install Gradle and continue with build
ENV GRADLE_VERSION=8.12
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip && \
    rm /tmp/gradle-${GRADLE_VERSION}-bin.zip
ENV PATH="${PATH}:/opt/gradle/gradle-${GRADLE_VERSION}/bin"

COPY --from=frontend-builder /app/frontend/dist/ ./src/main/resources/static/

RUN gradle build -x test --no-daemon

EXPOSE 8080

CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]
