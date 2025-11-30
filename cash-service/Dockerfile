FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
#COPY target/cash-service-*.jar app.jar
COPY app.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
