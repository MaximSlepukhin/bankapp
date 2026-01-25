FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY target/notifications-service-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
