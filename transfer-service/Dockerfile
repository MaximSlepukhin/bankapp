FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY target/transfer-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
