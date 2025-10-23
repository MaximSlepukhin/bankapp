FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

RUN apt-get update && apt-get install -y maven bash git && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY accounts-service/pom.xml accounts-service/pom.xml
COPY accounts-service/src accounts-service/src

RUN mvn clean package -DskipTests -f accounts-service/pom.xml

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/accounts-service/target/accounts-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
