FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache maven bash git

COPY pom.xml .
COPY exchange-generator-service/pom.xml exchange-generator-service/pom.xml
COPY exchange-generator-service/src exchange-generator-service/src

RUN mvn clean package -DskipTests -f exchange-generator-service/pom.xml

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/exchange-generator-service/target/exchange-generator-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java","-jar","app.jar"]
