# ---- Build stage ----
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

# Copy Gradle wrapper and configs first for caching
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./

# Download dependencies (skip tests)
RUN ./gradlew build -x test || return 0

# Copy source
COPY src src

# Build the jar
RUN ./gradlew clean bootJar -x test

# ---- Run stage ----
FROM eclipse-temurin:21-jdk AS runtime
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
