FROM node:21-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/project-frontend/package*.json ./
RUN npm install
COPY frontend/project-frontend/ ./

FROM gradle:8.13.0-jdk21-alpine
WORKDIR /app

RUN apk add --no-cache python3 py3-pip && \
ln -sf python3 /usr/bin/python && \
apk add --no-cache tree

# COPY App/ ./App/
# COPY gradle/ ./gradle/
# COPY gradlew build.gradle.kts settings.gradle.kts ./

CMD ["sh", "-c", "ls"]

