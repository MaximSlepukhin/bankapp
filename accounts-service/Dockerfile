FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
#COPY target/accounts-service-1.0-SNAPSHOT.jar app.jar
COPY app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

## === STAGE 1: Build ===
#FROM maven:3.9.6-eclipse-temurin-21 AS builder
#WORKDIR /app
#COPY . .
#RUN mvn -B clean package -DskipTests
#
## === STAGE 2: Run ===
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#EXPOSE 8081
#
#COPY --from=builder /app/target/accounts-service-1.0-SNAPSHOT.jar app.jar
#
#ENTRYPOINT ["java", "-jar", "app.jar"]
