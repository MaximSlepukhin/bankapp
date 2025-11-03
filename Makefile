# ================================
# 🏦 BankApp Makefile
# ================================

# Вспомогательные переменные
COMPOSE = docker compose
WAIT_FOR = bash -c 'until curl -sSf $$1 >/dev/null; do echo "⏳ Waiting for $$1..."; sleep 5; done'
KEYCLOAK_URL = http://localhost:8082/realms/master
DISCOVERY_URL = http://localhost:8761/actuator/health

# --------------------------------
# 🚀 Основные команды
# --------------------------------

## Собрать и запустить все контейнеры в нужном порядке
all: up-postgres-keycloak up-keycloak wait-keycloak up-postgres up-config-server up-discovery wait-discovery up-services up-gateway up-front

## Остановить все контейнеры
down:
	$(COMPOSE) down

## Пересобрать все сервисы и перезапустить
rebuild:
	$(COMPOSE) down
	make all

## Показать статус контейнеров
ps:
	$(COMPOSE) ps

## Просмотреть логи всех контейнеров
logs:
	$(COMPOSE) logs -f

## Удалить все образы и контейнеры проекта
clean:
	$(COMPOSE) down -v --remove-orphans
	docker system prune -f

# --------------------------------
# 🧱 Запуск по шагам
# --------------------------------

up-postgres-keycloak:
	@echo "🐘 Starting PostgreSQL for Keycloak..."
	$(COMPOSE) up -d --build postgres-keycloak

up-keycloak:
	@echo "🦸 Starting Keycloak..."
	$(COMPOSE) up -d --build keycloak

wait-keycloak:
	@echo "⏳ Waiting for Keycloak to be ready..."
	@until curl -sSf $(KEYCLOAK_URL) >/dev/null 2>&1; do \
		echo "   Keycloak not ready yet..."; sleep 10; \
	done
	@echo "✅ Keycloak is ready!"

up-postgres:
	@echo "🐘 Starting PostgreSQL for bank services..."
	$(COMPOSE) up -d --build postgres

up-config-server:
	@echo "⚙️ Starting Config Server..."
	$(COMPOSE) up -d --build config-server

up-discovery:
	@echo "🔍 Starting Discovery Server..."
	$(COMPOSE) up -d --build discovery-server

wait-discovery:
	@echo "⏳ Waiting for Discovery Server..."
	@until curl -sSf $(DISCOVERY_URL) >/dev/null 2>&1; do \
		echo "   Discovery not ready yet..."; sleep 5; \
	done
	@echo "✅ Discovery Server is ready!"

up-services:
	@echo "💼 Starting core services..."
	$(COMPOSE) up -d --build accounts-service cash-service transfer-service exchange-service blocker-service notifications-service exchange-generator-service

up-gateway:
	@echo "🌐 Starting API Gateway..."
	$(COMPOSE) up -d --build gateway

up-front:
	@echo "🖥️ Starting Front UI..."
	$(COMPOSE) up -d --build front-ui
	@echo "✅ All services are up and running!"
