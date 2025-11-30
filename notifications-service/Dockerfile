FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Копируем fat-jar с зависимостями
#COPY target/notifications-service-1.0-SNAPSHOT.jar app.jar
COPY app.jar app.jar

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
