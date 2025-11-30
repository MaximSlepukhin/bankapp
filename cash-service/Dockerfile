FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
