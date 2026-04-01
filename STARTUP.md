# Инструкция по запуску bankapp

## Предварительные требования
- Docker Desktop запущен (лимит памяти 13.5 GB)
- Minikube установлен
- Helm установлен
- Maven установлен (`/Users/maksim/apps/apache-maven-3.9.9/bin/mvn`)

---

## ⚠️ Важно перед запуском

Убедись что в `~/.docker/config.json` **нет** строки `"credsStore": "desktop"` — она ломает docker build в контексте Minikube.
Файл должен выглядеть так:
```json
{
    "auths": {},
    "currentContext": "desktop-linux",
    "plugins": {
        "-x-cli-hints": {
            "enabled": "true"
        }
    },
    "features": {
        "hooks": "true"
    }
}
```

---

## Шаг 1 — Запуск Minikube

```bash
minikube start --memory=12288
```

Если запускаешь впервые или после `minikube delete`:
```bash
minikube delete
minikube start --memory=12288
```

---

## Шаг 2 — Создание namespace

```bash
kubectl create namespace dev --dry-run=client -o yaml | kubectl apply -f -
kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -
```

---

## Шаг 3 — Сборка Maven

```bash
cd /Users/maksim/Desktop/All/bankapp
/Users/maksim/apps/apache-maven-3.9.9/bin/mvn clean package -DskipTests
```

---

## Шаг 4 — Сборка Docker образов в Minikube

```bash
eval $(minikube docker-env)
for s in accounts-service blocker-service cash-service exchange-generator-service exchange-service front-ui notifications-service transfer-service; do
  docker build -t ${s}:latest -f /Users/maksim/Desktop/All/bankapp/${s}/Dockerfile /Users/maksim/Desktop/All/bankapp/${s}
done
```

---

## Шаг 5 — Deploy ELK

```bash
helm repo add elastic https://helm.elastic.co
helm repo update

# Elasticsearch
helm upgrade --install elasticsearch elastic/elasticsearch \
  --namespace default \
  -f /Users/maksim/Desktop/All/bankapp/helm/bankapp/charts/elasticsearch/values.yaml \
  --timeout 600s

# Logstash (деплоим через kubectl, т.к. Helm chart недоступен)
kubectl apply -f /Users/maksim/Desktop/All/bankapp/helm/bankapp/charts/logstash/logstash-manifest.yaml

# Kibana (деплоим через kubectl, т.к. Helm chart нестабилен)
# Сначала чистим мусор от предыдущих попыток
kubectl delete deployment kibana-kibana -n default 2>/dev/null || true
kubectl delete secret kibana-kibana-es-token --namespace default 2>/dev/null || true
kubectl delete all -l app=kibana --namespace default 2>/dev/null || true
kubectl apply -f /Users/maksim/Desktop/All/bankapp/helm/bankapp/charts/kibana/kibana-manifest.yaml
```

---

## Шаг 6 — Deploy Kafka

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

helm upgrade --install my-kafka bitnami/kafka \
  --namespace default \
  --set image.repository=bitnamilegacy/kafka \
  --set image.tag=4.0.0 \
  --set replicaCount=1 \
  --set auth.enabled=false \
  --set kraft.enabled=false \
  --set zookeeper.enabled=true \
  --set listeners.client.protocol=PLAINTEXT \
  --set listeners.interbroker.protocol=PLAINTEXT \
  --wait --timeout 300s

# Создание топиков
kubectl delete pod kafka-client -n default --ignore-not-found
kubectl run kafka-client --restart=Never --image=bitnamilegacy/kafka:4.0.0 --namespace default -- sleep infinity
kubectl wait --for=condition=Ready pod/kafka-client -n default --timeout=180s

for topic in exchange-rates notifications logs; do
  kubectl exec -n default kafka-client -- kafka-topics.sh \
    --bootstrap-server my-kafka.default.svc.cluster.local:9092 \
    --create --if-not-exists \
    --topic $topic \
    --partitions 1 \
    --replication-factor 1
done
```

---

## Шаг 7 — Deploy баз данных

```bash
cd /Users/maksim/Desktop/All/bankapp
helm upgrade --install accounts-db ./helm/bankapp/charts/accounts-db --namespace dev --wait --timeout 300s
helm upgrade --install keycloak-db ./helm/bankapp/charts/keycloak-db --namespace dev --wait --timeout 300s
```

---

## Шаг 8 — Deploy микросервисов и Keycloak

```bash
cd /Users/maksim/Desktop/All/bankapp
for s in accounts-service blocker-service cash-service exchange-generator-service exchange-service front-ui notifications-service transfer-service keycloak; do
  helm upgrade --install $s ./helm/bankapp/charts/$s --namespace dev --timeout 300s
done
```

### ⚠️ Создать secrets (если поды в статусе CreateContainerConfigError)

```bash
kubectl create secret generic notifications-db-secret \
  --from-literal=POSTGRES_USERNAME="admin" \
  --from-literal=POSTGRES_PASSWORD="password" \
  --from-literal=KEYCLOAK_CLIENTSECRET="z1GkjE1Vi9Ba1B91tFPjZtg6CGQiw5ea" \
  --namespace dev

kubectl create secret generic transfer-service-secret \
  --from-literal=KEYCLOAK_CLIENTSECRET="RMU6Vb96Ay0fWnLJ6WEBiyt8yjdjKk24" \
  --namespace dev
```

---

## Шаг 9 — Deploy Zipkin

```bash
cd /Users/maksim/Desktop/All/bankapp
helm upgrade --install zipkin ./helm/bankapp/charts/zipkin --namespace monitoring --timeout 300s
```

---

## Шаг 10 — Deploy Prometheus + Grafana

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm upgrade --install prometheus-stack prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --set grafana.adminPassword=admin \
  --set grafana.adminUser=admin \
  --set prometheus.prometheusSpec.podMonitorSelectorNilUsesHelmValues=false \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
  --timeout 300s

# ServiceMonitors для микросервисов
kubectl apply -f /Users/maksim/Desktop/All/bankapp/helm/bankapp/charts/prometheus/templates/servicemonitor.yaml
```

---

## Проверка что всё работает

```bash
kubectl get pods -A
```

Все поды должны быть в статусе `Running`.

---

## Доступ к компонентам (каждую команду в отдельном терминале)

```bash
# Front UI — http://localhost:8081
kubectl port-forward -n dev svc/front-ui 8081:8080

# Keycloak — http://localhost:8080
kubectl port-forward -n dev svc/keycloak 8080:80

# Zipkin — http://localhost:9411
kubectl port-forward -n monitoring svc/zipkin 9411:9411

# Kibana — http://localhost:5601
kubectl port-forward -n default svc/kibana 5601:5601

# Prometheus — http://localhost:9090
kubectl port-forward -n monitoring svc/prometheus-stack-kube-prom-prometheus 9090:9090

# Grafana — http://localhost:3000 (admin/admin)
kubectl port-forward -n monitoring svc/prometheus-stack-grafana 3000:80
```

---

## Если что-то пошло не так

### Helm релиз завис в pending
```bash
helm uninstall <release-name> -n <namespace>
```

### Под не стартует — смотрим причину
```bash
kubectl describe pod <pod-name> -n <namespace>
```

### Логи пода
```bash
kubectl logs <pod-name> -n <namespace>
```

### Полный сброс
```bash
minikube delete
minikube start --memory=12288
```

---

## Быстрый старт (если кластер уже задеплоен)

### Шаг 1 — Убедиться что все поды подняты

```bash
kubectl get pods --all-namespaces
```

Все поды должны быть в статусе `1/1 Running` (кроме `kafka-client` — он одноразовый).

### Шаг 2 — Пробросить все порты одной командой

```bash
kubectl port-forward -n dev svc/front-ui 8081:8080 &
kubectl port-forward -n dev svc/keycloak 8080:80 &
kubectl port-forward -n monitoring svc/zipkin 9411:9411 &
kubectl port-forward -n default svc/kibana 5601:5601 &
kubectl port-forward -n monitoring svc/prometheus-stack-kube-prom-prometheus 9090:9090 &
kubectl port-forward -n monitoring svc/prometheus-stack-grafana 3000:80 &
```

### Доступные сервисы

| Сервис | URL |
|---|---|
| Front UI | http://localhost:8081/signup |
| Keycloak | http://localhost:8080 |
| Zipkin | http://localhost:9411 |
| Kibana | http://localhost:5601 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 (логин: admin, пароль: admin) |

### Остановить все port-forward

```bash
pkill -f "kubectl port-forward"
```
