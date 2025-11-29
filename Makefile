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

# --------------------------------
# 🧩 Работа с Helm Chart'ами
# --------------------------------

## Создать Helm chart из шаблона
create-helm-chart:
	@if [ -z "$(name)" ]; then \
		echo "❌ Укажи имя сервиса, пример: make create-helm-chart name=accounts-service"; \
		exit 1; \
	fi
	@mkdir -p helm/$(name)
	@cp -r helm-template/* helm/$(name)/
	@find helm/$(name) -type f -exec sed -i '' "s/{{ .Chart.Name }}/$(name)/g" {} +
	@echo "✅ Helm chart создан: helm/$(name)"

## Установить Helm chart
helm-install:
	@if [ -z "$(name)" ]; then \
		echo "❌ Укажи имя чарта: make helm-install name=accounts-service"; \
		exit 1; \
	fi
	@helm install $(name) ./helm/$(name)
	@echo "🚀 Установлен чарт $(name)"

## Обновить Helm chart
helm-upgrade:
	@if [ -z "$(name)" ]; then \
		echo "❌ Укажи имя чарта: make helm-upgrade name=accounts-service"; \
		exit 1; \
	fi
	@helm upgrade $(name) ./helm/$(name)
	@echo "🔄 Обновлён чарт $(name)"

## Удалить Helm chart
helm-uninstall:
	@if [ -z "$(name)" ]; then \
		echo "❌ Укажи имя чарта: make helm-uninstall name=accounts-service"; \
		exit 1; \
	fi
	@helm uninstall $(name)
	@echo "🗑️ Удалён чарт $(name)"

## Развернуть все Helm чарты
helm-up:
	helm install config-server ./helm/config-server || helm upgrade config-server ./helm/config-server
	helm install discovery-server ./helm/discovery-server || helm upgrade discovery-server ./helm/discovery-server
	helm install accounts-service ./helm/accounts-service || helm upgrade accounts-service ./helm/accounts-service
	helm install cash-service ./helm/cash-service || helm upgrade cash-service ./helm/cash-service
	helm install transfer-service ./helm/transfer-service || helm upgrade transfer-service ./helm/transfer-service
	helm install exchange-service ./helm/exchange-service || helm upgrade exchange-service ./helm/exchange-service
	helm install blocker-service ./helm/blocker-service || helm upgrade blocker-service ./helm/blocker-service
	helm install notifications-service ./helm/notifications-service || helm upgrade notifications-service ./helm/notifications-service
	helm install exchange-generator-service ./helm/exchange-generator-service || helm upgrade exchange-generator-service ./helm/exchange-generator-service
	helm install gateway ./helm/gateway || helm upgrade gateway ./helm/gateway
	helm install front-ui ./helm/front-ui || helm upgrade front-ui ./helm/front-ui

## Удалить все релизы
helm-down:
	helm uninstall config-server discovery-server accounts-service cash-service transfer-service exchange-service blocker-service notifications-service exchange-generator-service gateway front-ui || true

SERVICES = accounts-service cash-service transfer-service exchange-service blocker-service notifications-service exchange-generator-service gateway front-ui config-server discovery-server

# -------------------------------
# 1️⃣ Сборка всех образов
# -------------------------------
build-all:
	@for svc in $(SERVICES); do \
	  echo "🚀 Building $$svc..."; \
	  docker build -t $$svc:latest -f $$svc/Dockerfile .; \
	done
	@echo "✅ Все образы собраны!"

# -------------------------------
# 1️⃣ Сборка всех образов
# -------------------------------
build-all:
	@for svc in $(SERVICES); do \
	  echo "🚀 Building $$svc..."; \
	  docker build -t $$svc:latest -f $$svc/Dockerfile .; \
	done
	@echo "✅ Все образы собраны!"

# -------------------------------
# 2️⃣ Загрузка собранных образов в Minikube
# -------------------------------
load-all:
	@for svc in $(SERVICES); do \
	  echo "📦 Loading $$svc into Minikube..."; \
	  minikube image load $$svc:latest; \
	done
	@echo "✅ Все образы загружены в Minikube!"

# -------------------------------
# 3️⃣ Перезапуск всех деплойментов
# -------------------------------
deploy-all:
	@for svc in $(SERVICES); do \
	  echo "🚢 Deploying $$svc via Helm..."; \
	  helm upgrade --install $$svc ./helm/$$svc; \
	done
	@echo "✅ Все Helm-чарты установлены!"

# -------------------------------
# 4️⃣ Деплой всех сервисов через Helm (без namespace)
# -------------------------------
redeploy-all:
	@for svc in $(SERVICES); do \
	  echo "🔄 Redeploying $$svc..."; \
	  kubectl rollout restart deployment $$svc; \
	done
	@echo "✅ Все деплойменты перезапущены!"

