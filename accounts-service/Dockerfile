# ------------------------------
# Stage 1: Build JAR
# ------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Устанавливаем Maven и bash
RUN apk add --no-cache maven bash git

# Копируем только POM и исходники модуля
COPY pom.xml .
COPY accounts-service/pom.xml accounts-service/pom.xml
COPY accounts-service/src accounts-service/src

# Сборка JAR без тестов
RUN mvn clean package -DskipTests -f accounts-service/pom.xml

# ------------------------------
# Stage 2: Run JAR
# ------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/accounts-service/target/accounts-service-1.0-SNAPSHOT.jar app.jar

# Порт сервиса
EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
