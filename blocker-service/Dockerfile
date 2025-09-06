FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY blocker-service/target/blocker-service-*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
