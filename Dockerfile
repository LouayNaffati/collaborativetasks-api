# ---- Build stage ----
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Copy Gradle files first for dependency caching
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./

# Download dependencies
RUN ./gradlew build -x test || return 0

# Copy source
COPY src src

# Build the JAR
RUN ./gradlew clean bootJar -x test

# ---- Run stage ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the same port as in application.properties
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
