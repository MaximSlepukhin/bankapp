# ------------------------------
# Stage 1: Build JAR
# ------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Устанавливаем Maven и bash
RUN apk add --no-cache maven bash git

# Копируем POM файлы
COPY pom.xml .
COPY accounts-service/pom.xml accounts-service/pom.xml

# Копируем исходники
COPY . .

# Сборка JAR без запуска тестов
RUN mvn clean package -DskipTests

# ------------------------------
# Stage 2: Run JAR
# ------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем собранный JAR из первого этапа
COPY --from=build /app/accounts-service/target/accounts-service-1.0-SNAPSHOT.jar app.jar

# Открываем порт сервиса
EXPOSE 8081

# Запуск сервиса
ENTRYPOINT ["java","-jar","app.jar"]