# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy everything needed for Maven build
COPY pom.xml .
COPY src ./src

# Build Spring Boot jar
RUN mvn clean package -DskipTests

# ---------- Run stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# App directories for file-based H2 and logs
RUN mkdir -p /app/data /app/logs /app/logs/archived

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]