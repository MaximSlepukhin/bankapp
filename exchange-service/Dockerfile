FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY exchange-service/target/exchange-service-*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
