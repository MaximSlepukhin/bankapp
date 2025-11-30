#FROM eclipse-temurin:21-jdk AS build
#WORKDIR /app
#RUN apt-get update && apt-get install -y maven bash git && rm -rf /var/lib/apt/lists/*
#
#COPY pom.xml .
#COPY exchange-generator-service/pom.xml exchange-generator-service/pom.xml
#COPY exchange-generator-service/src exchange-generator-service/src
#
#RUN mvn clean package -DskipTests -f exchange-generator-service/pom.xml
#
#FROM eclipse-temurin:21-jre-alpine
#WORKDIR /app
#COPY --from=build /app/exchange-generator-service/target/exchange-generator-service-1.0-SNAPSHOT.jar app.jar
#
#EXPOSE 8084
#ENTRYPOINT ["java","-jar","app.jar"]
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Копируем готовый JAR (собранный Jenkins)
COPY app.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java","-jar","app.jar"]
