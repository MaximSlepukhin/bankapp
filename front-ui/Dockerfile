# Stage 1: Build JAR
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven bash git

COPY pom.xml .
COPY front-ui/pom.xml front-ui/pom.xml
COPY front-ui/src front-ui/src

RUN mvn clean package -DskipTests -f front-ui/pom.xml

# Stage 2: Run JAR
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/front-ui/target/front-ui-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
