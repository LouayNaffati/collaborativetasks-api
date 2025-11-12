# ---- Run stage ----
FROM eclipse-temurin:21-jdk AS runtime
WORKDIR /app

# Copy built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the same port as in application.properties
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
