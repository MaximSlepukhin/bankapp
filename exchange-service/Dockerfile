FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY target/exchange-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
