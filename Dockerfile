# Single-image build: the React app is compiled and served as static files by the
# Spring Boot backend, so the whole project deploys as one container on one port.

# 1) Build the frontend
FROM node:22-alpine AS frontend
WORKDIR /fe
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# 2) Build the backend, bundling the frontend into its static resources
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /be
COPY backend/pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /fe/dist ./src/main/resources/static
RUN mvn -B -q clean package -DskipTests

# 3) Slim runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /be/target/*.jar app.jar
EXPOSE 8080
# Hosts like Render/Fly inject $PORT; fall back to 8080 locally.
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
