
# 🏦 BankApp — Микросервисное банковское приложение "Банк"

Микросервисный проект с использованием **Spring Boot**, **Spring Cloud**, **Keycloak**, **PostgreSQL**,
**Docker Compose**, **Eureka Discovery**, **API Gateway** и **Frontend UI**.

---

##  Локальный запуск проекта

### 📋 Требования

Перед запуском убедись, что установлены:

- **Docker**
- **Docker Compose**
- **Make** (для удобного запуска)
- **Java** ≥ 21

---

##  1. Сборка проекта

Сначала собери все JAR-файлы проекта:

```bash
mvn clean install -DskipTests
```

---

## 🏗️ 2. Запуск контейнеров

### ✅ Вариант 1 — через Makefile (рекомендуется)

Просто выполни:

```bash
make all
```

Эта команда автоматически:

1. Поднимет **PostgreSQL** для Keycloak
2. Поднимет **Keycloak** и дождётся его готовности
3. Импортирует `bank-realm.json`
4. Поднимет **PostgreSQL** для банковских сервисов
5. Запустит **Config Server** и **Discovery Server**
6. Запустит все микросервисы:  
   `accounts-service`, `cash-service`, `transfer-service`,  
   `exchange-service`, `blocker-service`, `notifications-service`, `exchange-generator-service`
7. Запустит **Gateway** и **Frontend UI**

## ✅ Готово!

После запуска можно открыть:
- Frontend → [http://localhost:8080/signup]
- Авторизация через Keycloak (`bank-realm`)
- Все микросервисы зарегистрированы в Eureka и доступны через Gateway.

---

### ⚙️ Вариант 2 — вручную

Если нет `make`, можно запускать вручную:

```bash
docker compose up -d --build postgres-keycloak
docker compose up -d --build keycloak
# дождаться готовности Keycloak (~30 секунд)
docker compose up -d --build postgres
docker compose up -d --build config-server discovery-server
docker compose up -d --build accounts-service cash-service transfer-service exchange-service blocker-service notifications-service exchange-generator-service
docker compose up -d --build gateway front-ui
```

---

## 🌍 3. Основные URL

| Компонент | URL |
|------------|-----|
| 🦸 Keycloak Admin Console | [http://localhost:8082/admin](http://localhost:8082/admin) |
| ⚙️ Config Server | [http://localhost:8888](http://localhost:8888) |
| 🔍 Discovery Server (Eureka) | [http://localhost:8761](http://localhost:8761) |
| 🌐 Gateway API | [http://localhost:8090](http://localhost:8090) |
| 🖥️ Frontend UI | [http://localhost:8080](http://localhost:8080) |

🔑 **Keycloak credentials:**
```
Username: admin
Password: admin
```

---

## 🧱 4. Описание микросервисов

| Сервис | Назначение | Порт |
|---------|-------------|------|
| 🐘 **postgres** | БД для банковских сервисов | 5433 |
| 🐘 **postgres-keycloak** | БД для Keycloak | 5434 |
| 🦸 **keycloak** | OAuth2 / OpenID Connect сервер авторизации | 8082 |
| ⚙️ **config-server** | Spring Cloud Config Server | 8888 |
| 🔍 **discovery-server** | Eureka Server для регистрации микросервисов | 8761 |
| 💳 **accounts-service** | Управление банковскими счетами | 8081 |
| 💰 **cash-service** | Наличные операции | 8091 |
| 💸 **transfer-service** | Денежные переводы | 8083 |
| 💱 **exchange-service** | Обмен валют | 8085 |
| 🚫 **blocker-service** | Блокировка счетов и карт | 8086 |
| 📢 **notifications-service** | Уведомления | 8087 |
| 📊 **exchange-generator-service** | Генерация валютных курсов | 8088 |
| 🌐 **gateway** | API Gateway (входная точка для фронта и клиентов) | 8090 |
| 🖥️ **front-ui** | Веб-интерфейс приложения | 8080 |

---

## 🔑 5. Импорт Keycloak Realm

При первом старте Keycloak автоматически импортирует `bank-realm.json` из директории:

```
./keycloak-export → /opt/keycloak/data/import
```

Это создаёт:
- Realm: `bank-realm`
- Клиентов (front-ui и др.)
- Роли, пользователей и настройки аутентификации.

---

## 🧰 6. Полезные команды Makefile

| Команда | Описание |
|----------|-----------|
| `make all` | Полный запуск всех сервисов в нужном порядке |
| `make down` | Остановка всех контейнеров |
| `make rebuild` | Полная пересборка и перезапуск |
| `make ps` | Проверка статуса всех контейнеров |
| `make logs` | Просмотр логов |
| `make clean` | Полное удаление контейнеров и volume’ов |

---

## 🧹 7. Очистка окружения

Если нужно удалить всё (контейнеры, volume и кэш):

```bash
make clean
```

или

```bash
docker compose down -v --remove-orphans
docker system prune -f
```

---

