FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY transfer-service/target/transfer-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
