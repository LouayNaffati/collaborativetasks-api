# ---- Build stage ----
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Make wrapper executable
COPY gradlew .
RUN chmod +x gradlew

# Copy Gradle configs first for dependency caching
COPY build.gradle settings.gradle ./
COPY gradle gradle

# Download dependencies (skip tests)
RUN ./gradlew build -x test --no-daemon

# Copy source
COPY src src

# Build the JAR
RUN ./gradlew clean bootJar -x test --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:21-jdk AS runtime
WORKDIR /app

# Copy built jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
