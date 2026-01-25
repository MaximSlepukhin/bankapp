FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY target/exchange-generator-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","app.jar"]
