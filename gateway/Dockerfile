# Stage 1: Build JAR
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven bash git

# Копируем корневой POM и модуль gateway
COPY pom.xml pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY gateway/src gateway/src

RUN mvn clean package -DskipTests -f gateway/pom.xml

# Stage 2: Run JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/gateway/target/gateway-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
