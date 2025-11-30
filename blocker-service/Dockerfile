FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
#COPY target/blocker-service-*.jar app.jar
COPY app.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
