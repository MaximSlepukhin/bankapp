# BankApp — микросервисное приложение «Банк»

Микросервисное приложение, разработанное в рамках **11-го спринта**.  
Приложение развёртывается в **Kubernetes**, взаимодействие между частью микросервисов реализовано через **Apache Kafka**.

---
## Сборка и запуск локально (development)

### Требования
- Docker
- Minikube
- kubectl
- Helm
- Maven
- Jenkins


## Архитектура приложения

### Микросервисы
- `front-ui`
- `accounts-service`
- `cash-service`
- `transfer-service`
- `exchange-service`
- `exchange-generator-service`
- `blocker-service`
- `notifications-service`
- `keycloak`

---

## Взаимодействие через Apache Kafka

| Producer → Consumer | Topic | Гарантия доставки |
|--------------------|-------|-------------------|
| Accounts → Notifications | `notifications` | at least once |
| Cash → Notifications | `notifications` | at least once |
| Transfer → Notifications | `notifications` | at least once |
| Exchange Generator → Exchange | `exchange-rates` | at most once (ordered) |

- Взаимодействие между `exchange-generator-service` и `exchange-service` реализовано только через Kafka

---

## Apache Kafka

- Развёртывание: **Helm (Bitnami Kafka)**
- Namespace: `default`
- Brokers: **1**
- Zookeeper: включён
- KRaft: отключён
- Протокол: `PLAINTEXT`
- Хранение данных: `PersistentVolumeClaim`

### Kafka topics
- `notifications`
- `exchange-rates`

---

## CI/CD (Jenkins)

Развёртывание и обновление приложения выполняется через Jenkins pipeline.

Pipeline выполняет:
1. Проверку версий инструментов
2. Развёртывание Apache Kafka через Helm
3. Создание Kafka topics
4. Сборку микросервисов с помощью Maven
5. Сборку Docker-образов внутри Minikube
6. Развёртывание баз данных
7. Развёртывание микросервисов через Helm

`Jenkinsfile` хранится в репозитории и может быть использован в CI/CD Jenkins.

---

### Запуск Minikube
```bash
minikube start
