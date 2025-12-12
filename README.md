# BankApp - Микросервисное банковское приложение

BankApp - это микросервисное банковское приложение

### Обязательные компоненты:
   docker --version
   minikube version
   helm version
   mvn --version
   java -version
   git --version

## 📦 Описание Helm чартов

Проект использует зонтичный (umbrella) Helm чарт `bankapp`, который объединяет все микросервисы и их зависимости.

### Структура чартов

```
helm/bankapp/
├── Chart.yaml              # Главный чарт с зависимостями
├── values-dev.yaml         # Конфигурация для dev окружения
├── values-test.yaml        # Конфигурация для test окружения
├── values-prod.yaml        # Конфигурация для prod окружения
└── charts/                 # Sub-charts (зависимости)
    ├── accounts-db/        # PostgreSQL база данных для accounts-service
    ├── accounts-service/    # Сервис управления аккаунтами
    ├── blocker-service/    # Сервис блокировки операций
    ├── cash-service/       # Сервис работы с наличными
    ├── exchange-generator-service/  # Генератор курсов валют
    ├── exchange-service/   # Сервис обмена валют
    ├── front-ui/           # Frontend приложение
    ├── keycloak/           # Сервис аутентификации и авторизации
    ├── keycloak-db/        # PostgreSQL база данных для Keycloak
    ├── notifications-service/  # Сервис уведомлений
    └── transfer-service/   # Сервис переводов
```

### Окружения
Проект поддерживает три окружения:
- **dev** - окружение разработки
- **test** - тестовое окружение
- **prod** - продакшн окружение

Каждое окружение имеет свой namespace в Kubernetes и свой файл values.
## 🚀 Порядок запуска

### Шаг 1: Клонирование проекта
git clone <repository-url>


### Шаг 2: Запуск Minikube
minikube start --driver=docker --memory=6g --cpus=4
# Проверка статуса
minikube status
kubectl get nodes

### Шаг 3: Настройка Docker окружения для Minikube

```bash
# Настройка Docker для использования Minikube registry
eval $(minikube docker-env)
```

### Шаг 4: Создание namespaces

```bash
# Создание namespaces для разных окружений
kubectl apply -f namespaces.yaml

# Проверка созданных namespaces
kubectl get namespaces
```

### Шаг 5: Сборка Docker образов

Для каждого сервиса необходимо собрать Docker образ. Пример для всех сервисов:
mvn clean package
eval $(minikube docker-env)
docker build -t accounts-service:latest -f accounts-service/Dockerfile accounts-service

# Blocker Service
mvn clean package
docker build -t blocker-service:latest -f blocker-service/Dockerfile blocker-service

# Cash Service
mvn clean package
eval $(minikube docker-env)
docker build -t cash-service:latest -f cash-service/Dockerfile cash-service

# Exchange Generator Service
mvn clean package
eval $(minikube docker-env)
docker build -t exchange-generator-service:latest -f exchange-generator-service/Dockerfile exchange-generator-service

# Exchange Service
mvn clean package
eval $(minikube docker-env)
docker build -t exchange-service:latest -f exchange-service/Dockerfile exchange-service

# Notifications Service
mvn clean package
eval $(minikube docker-env)
docker build -t notifications-service:latest -f notifications-service/Dockerfile notifications-service

# Transfer Service
mvn clean package
eval $(minikube docker-env)
docker build -t transfer-service:latest -f transfer-service/Dockerfile transfer-service

# Front UI
mvn clean package
eval $(minikube docker-env)
docker build -t front-ui:latest -f front-ui/Dockerfile front-ui
```
```
### Шаг 6: Установка приложения через Helm

#### Вариант 1: Установка всего приложения одним чартом

helm install bankapp . --namespace dev --create-namespace -f values-dev.yaml

#### Вариант 2: Установка сервисов по отдельности (рекомендуется для первого запуска)

**1. Установка баз данных (сначала):**
helm install accounts-db . --namespace dev --force
helm install keycloak-db . --namespace dev --force


**2. Установка Keycloak:**

```bash
helm install keycloak . --namespace dev --force
```

**3. Установка микросервисов:**

```bash
# Accounts Service
helm install accounts-service . --namespace dev --force

# Blocker Service
helm install blocker-service . --namespace dev --force

# Cash Service
helm install cash-service . --namespace dev --force

# Exchange Generator Service
helm install exchange-generator-service . --namespace dev --force

# Exchange Service
helm install exchange-service . --namespace dev --force

# Notifications Service
helm install notifications-service . --namespace dev --force

# Transfer Service
helm install transfer-service . --namespace dev --force

# Front UI
helm install front-ui . --namespace dev --force
```

### Шаг 8: Проверка статуса подов

```bash
# Проверка всех подов в namespace dev
kubectl get pods -n dev

# Проверка всех ресурсов
kubectl get all -n dev
```

## ✅ Проверка работоспособности

### Проверка статуса сервисов

```bash
# Список всех установленных Helm релизов
helm list -n dev

# Статус подов
kubectl get pods -n dev

# Статус сервисов
kubectl get svc -n dev

# Логи конкретного сервиса
kubectl logs -n dev deployment/accounts-service-accounts
```

### Проброс портов для доступа к сервисам

```bash
# Front UI (порт 8081)
kubectl port-forward -n dev svc/front-ui 8081:8080

# Keycloak (порт 8080)
kubectl port-forward -n dev svc/keycloak 8080:80
```

После проброса портов:
- Front UI будет доступен по адресу: http://localhost:8081
- Keycloak будет доступен по адресу: http://localhost:808
