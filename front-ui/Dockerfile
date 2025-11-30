FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENTRYPOINT ["java", "-jar", "app.jar"]
