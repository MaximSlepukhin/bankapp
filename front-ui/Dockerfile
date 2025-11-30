#FROM eclipse-temurin:21-jdk AS build
#WORKDIR /app
#
## Устанавливаем maven, bash и git
#RUN apt-get update && apt-get install -y maven bash git && rm -rf /var/lib/apt/lists/*
#
## Копируем корневой pom.xml (для parent)
#COPY pom.xml .
#
## Копируем pom.xml и src фронта
#COPY front-ui/pom.xml front-ui/pom.xml
#COPY front-ui/src front-ui/src
#
## Сборка front-ui
#RUN mvn clean package -DskipTests -f front-ui/pom.xml
#
## === STAGE 2: Run ===
#FROM eclipse-temurin:21-jre-alpine
#WORKDIR /app
#
## ✅ Включаем удалённый дебаг
#ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
#
## Копируем jar
#COPY --from=build /app/front-ui/target/front-ui-1.0-SNAPSHOT.jar app.jar
#
#EXPOSE 8080
#EXPOSE 5005
#
#ENTRYPOINT ["java", "-jar", "app.jar"]
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Копируем готовый JAR, который собрал Jenkins
COPY app.jar app.jar

EXPOSE 8080
# Включаем удалённый дебаг
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

ENTRYPOINT ["java", "-jar", "app.jar"]
