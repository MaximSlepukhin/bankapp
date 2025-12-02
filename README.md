
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


# 1️⃣ Основная структура
mkdir -p helm/bankapp/{templates,charts}

# 2️⃣ Основные файлы чарта
touch helm/bankapp/{Chart.yaml,values.yaml,values-dev.yaml,values-test.yaml,values-prod.yaml}
touch helm/bankapp/templates/{_helpers.tpl,ingress.yaml,configmap.yaml,secrets.yaml,postgres-statefulset.yaml,namespace.yaml}

# 3️⃣ Сабчарты для микросервисов
for svc in accounts-service cash-service transfer-service exchange-service exchange-generator-service blocker-service notifications-service front-ui; do
mkdir -p helm/bankapp/charts/$svc/templates
touch helm/bankapp/charts/$svc/{Chart.yaml,values.yaml}
touch helm/bankapp/charts/$svc/templates/{deployment.yaml,service.yaml}
done

# 4️⃣ Сабчарт для Keycloak (внутренний или заглушка, если потом подключишь внешний)
mkdir -p helm/bankapp/charts/keycloak/templates
touch helm/bankapp/charts/keycloak/{Chart.yaml,values.yaml}
touch helm/bankapp/charts/keycloak/templates/{deployment.yaml,service.yaml}

# 5️⃣ Тесты Helm (helm test)
mkdir -p helm/bankapp/templates/tests
touch helm/bankapp/templates/tests/{test-connection.yaml,test-pods.yaml}

# 6️⃣ Jenkinsfiles
touch Jenkinsfile
for svc in accounts-service cash-service transfer-service exchange-service exchange-generator-service blocker-service notifications-service front-ui; do
mkdir -p $svc
touch $svc/Jenkinsfile
done





## Требования

Для работы с данным проектом необходимо установить **Helm** версии **3.0** или выше. Чтобы проверить,
что Helm устанволен и работает корректно, выполните команду: helm version

Для локальной разработки и тестирования необходимо установить Minikube. Minikube создаст локальный кластер Kubernetes,
в котором будут развернуты все сервисы вашего приложения.

Убедитесь, что у вас установлен kubectl для взаимодействия с Kubernetes. Это нужно для управления вашим локальным кластером и развертываниями.
kubectl version --client

Для сборки Docker-образов, которые будут развернуты в Minikube, необходимо установить Docker.
docker --version

Убедитесь, что у вас установлен Git, чтобы клонировать репозиторий и работать с ним локально.
git --version


### Установка Helm

1. Скачайте и установите последнюю стабильную версию Helm для вашей операционной системы, следуя инструкциям на официальном сайте:

    - [Установка Helm для Linux](https://helm.sh/docs/intro/install/#from-script)
    - [Установка Helm для macOS](https://helm.sh/docs/intro/install/#from-homebrew)
    - [Установка Helm для Windows](https://helm.sh/docs/intro/install/#from-chocolatey)

2. Проверьте версию Helm, убедившись, что она соответствует требуемой:

   ```bash
   helm version


docker build -f Dockerfile.jenkins -t jenkins-k8s:latest .
docker images


docker run -d \
--name jenkins-k8s \
-p 8080:8080 \
-p 50000:50000 \
-v /Users/maksim/.kube:/var/jenkins_home/.kube:ro \
-v /Users/maksim/.minikube:/var/jenkins_home/.minikube:ro \
-v /var/run/docker.sock:/var/run/docker.sock \
-v jenkins_home:/var/jenkins_home \
jenkins-k8s:latest

docker exec jenkins-k8s cat /var/jenkins_home/secrets/initialAdminPassword
16ab5a5ec0284a159dbd6dd787850a85

docker stop jenkins-k8s
docker rm jenkins-k8s

docker start jenkins-k8s

docker pull maven:3.9.8-eclipse-temurin-21



1. Предварительные требования
   docker --version
   minikube version
   kubectl version --client
   helm version
   git --version
   java -version


2. Клонирование проекта
   git clone https://github.com/<ваш-username>/<название-проекта>.git
   cd <название-проекта>
   git checkout v2.0

3. Запуск Minikube
   minikube start --driver=docker --memory=6g --cpus=4
Проверка статуса:
   minikube status
   kubectl get nodes

4. Подготовка Helm
   helm repo update
5. Создать namespace в Kubernetes через YAML-манифест
   kubectl apply -f namespaces.yaml






1 Проверка статуса
minikube status

2 Запуск миникуба
minikube start --memory=8192 --cpus=4

3 Првоерка что все работает
kubectl get nodes


helm upgrade --install accounts-db ./helm/accounts-db --namespace default




minikube start
minikube status
minikube tunnel
nano ~/.docker/config.json
helm list -A
kubectl get pods -A
kubectl port-forward svc/front-ui 8081:8080
kubectl port-forward deployment/keycloak 8080:8080



eval $(minikube docker-env)
docker build -t exchange-generator-service:latest -f exchange-generator-service/Dockerfile .
helm install exchange-generator-service . --force
helm upgrade exchange-generator-service . --force
kubectl rollout restart deployment exchange-generator-service
kubectl get pods -A
kubectl logs


eval $(minikube docker-env)
docker build -t notifications-service:latest -f notifications-service/Dockerfile .
kubectl get pods -A
helm install notifications-service . --force
helm upgrade notifications-service . --force
kubectl logs
kubectl rollout restart deployment notifications-service-notifications-service


eval $(minikube docker-env)
docker build -t exchange-service:latest -f exchange-service/Dockerfile .
helm install exchange-service . --force
helm upgrade exchange-service . --force
kubectl get pods -A
kubectl rollout restart deployment exchange-service-exchange-service
kubectl logs

docker images
docker rmi exchange-service:latest



eval $(minikube docker-env)
docker build -t blocker-service:latest -f blocker-service/Dockerfile .
helm install blocker-service . --force
helm upgrade blocker-service . --force
kubectl get pods -A
kubectl logs
kubectl rollout restart deployment blocker-service-blocker-service



kubectl delete pod exchange-service-exchange-service-546dbcc759-8wtqg
kubectl delete deployment exchange-service-exchange-service


mvn clean package -DskipTests
eval $(minikube docker-env)
docker build -t transfer-service:latest -f transfer-service/Dockerfile .
helm install transfer-service . --force
helm upgrade transfer-service . --force
kubectl get pods -A
kubectl logs
kubectl delete deployment transfer-service -n default


eval $(minikube docker-env)
docker build -t cash-service:latest -f cash-service/Dockerfile .
helm install cash-service . --force
helm upgrade cash-service . --force
kubectl get pods -A
kubectl logs



minikube addons enable ingress
kubectl get pods -n ingress-nginx
sudo nano /etc/hosts
127.0.0.1 keycloak.local
minikube tunnel



kubectl get ns
helm list -A
kubectl get pods -A




eval $(minikube docker-env)
docker build -t front-ui:latest -f front-ui/Dockerfile .
docker images | grep front-ui

nano ~/.docker/config.json
minikube image load front-ui:latest



из front-ui
helm upgrade front-ui . --force
helm upgrade front-ui . --namespace default --set image.tag=latest
helm upgrade front-ui . --namespace default

Проверка  url
minikube service front-ui --url

Проверка логов
kubectl logs deploy/front-ui

Проверка статусов подов
kubectl get pods -n default

после этой команды у меня страница регистрации открывается по http://localhost:8080/signup
kubectl port-forward deployment/front-ui 8080:8080

minikube service --url front-ui


minikube service --url keycloak


helm upgrade accounts-service .
helm upgrade keycloak . -n default -f values.yaml



kubectl exec -it deploy/keycloak -n default -- bash
/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
/opt/keycloak/bin/kcadm.sh delete realms/bank-realm
/opt/keycloak/bin/kcadm.sh get realms


    пробрасываем порт!!!!!
kubectl port-forward svc/keycloak 9090:8080 -n default


    Убедись, что ConfigMap с твоим JSON актуален:
kubectl get configmap keycloak-realm-import -n default -o yaml

Там должен быть твой bank-realm-realm.json с нужными redirectUris.
А при необходимости указать hostname:

helm upgrade keycloak . -n default -f values.yaml --set keycloak.hostname="192.168.49.2"


!!!!!!!!!!!обязательно для запска!!!!!!!!
kubectl set env deployment/keycloak KC_HOSTNAME=localhost
kubectl rollout restart deployment/keycloak
kubectl rollout restart deployment keycloak --namespace=default

maksim@MacBook-Pro-Maksim bankapp % kubectl port-forward deployment/keycloak 8080:8080



kubectl port-forward svc/front-ui 8081:8080
kubectl port-forward pod/keycloak-5d6c5c49c9-cdm94 8080:8080



kubectl delete secret keycloak-tls-secret --namespace default

kubectl create secret tls keycloak-tls-secret \
--cert=./tls.crt \
--key=./tls.key \
--namespace default

kubectl get secret keycloak-tls-secret --namespace default -o yaml


kubectl create secret tls keycloak-tls-secret \
kubectl get secret keycloak-tls-secret --namespace default -o yaml


kubectl get pods --namespace=default
kubectl logs keycloak-<новый_pod_name> --namespace default


kubectl logs keycloak-64ff4db849-hzmqc --namespace=default




helm uninstall front-ui -n default

kubectl logs front-ui-54bfb846f7-bxq88

сделал optional для email и profile и в application оставил только openid


minikube addons enable registry
minikube addons enable registry-aliases
kubectl port-forward -n kube-system svc/registry 54611:80


docker network connect minikube jenkins-k8s
kubectl port-forward -n kube-system service/registry 5000:80
