# Используем базовый образ JDK 21
FROM eclipse-temurin:21-jdk

# Рабочая директория внутри контейнера
WORKDIR /app

# Копируем собранный JAR из target в контейнер
# Указываем точный путь после сборки Maven
COPY target/blocker-service-*.jar app.jar

# Открываем порт приложения
EXPOSE 8087

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
