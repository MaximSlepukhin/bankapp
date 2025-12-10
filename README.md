

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






kubectl apply -f namespaces.yaml
kubectl get namespaces


nano ~/.docker/config.json
FRONT-UI
cd front-ui
mvn clean package -DskipTests
cd ..
eval $(minikube docker-env)
docker build -t front-ui:latest -f front-ui/Dockerfile front-ui
cd helm/bankapp/charts/front-ui
minikube ssh
docker images | grep front-ui


из папки bankapp/helm/charts/front-ui
helm install front-ui . --namespace dev --force
helm upgrade front-ui . --namespace dev --force
kubectl rollout restart deployment front-ui -n dev
kubectl port-forward -n dev svc/front-ui 8081:8080


helm list
kubectl get pods
kubectl get svc

kubectl port-forward -n dev svc/front-ui 8081:8080



БАЗА ДАННЫХ ACCOUNTS-DB
cd helm/bankapp/charts/accounts-db
helm install accounts-db . --namespace dev --force
helm upgrade accounts-db . --namespace dev --force
kubectl rollout restart deployment accounts-service-accounts -n dev

kubectl run pg-client --rm -it --image=postgres:15 --namespace dev -- bash
kubectl delete pod pg-client -n dev
psql -h accounts-db-accounts-db.dev.svc.cluster.local -U accounts_user -d accountsdb
\dn
\dt accounts_service.*
SELECT * FROM accounts_service.users;
SELECT * FROM accounts_service.accounts;

DELETE FROM accounts_service.accounts WHERE owner_id IN (SELECT id FROM accounts_service.users);
DELETE FROM accounts_service.users;


SELECT *
FROM accounts_service.accounts
WHERE owner_id = (SELECT id FROM accounts_service.users WHERE login = 'Максим Слепухин');


БАЗА ДАННЫХ keycloak-db
cd helm/bankapp/charts/keycloak-db
helm install keycloak-db . --namespace dev --force
helm upgrade keycloak-db . --namespace dev --force
kubectl get all -n dev



BLOCKER-SERVICE
cd blocker-service
mvn clean package -DskipTests
cd ..
eval $(minikube docker-env)
docker build -t blocker-service:latest -f blocker-service/Dockerfile blocker-service
cd helm/bankapp/charts/blocker-service
helm install blocker-service . --namespace dev --force
helm upgrade blocker-service . --namespace dev --force


minikube ssh
docker images | grep blocker-service




kubectl get all -n dev



ACCOUNTS-SERVICE
cd accounts-service
mvn clean package -DskipTests
cd ..
eval $(minikube docker-env)
docker build -t accounts-service:latest -f accounts-service/Dockerfile accounts-service
docker images | grep accounts-service
cd helm/bankapp/charts/accounts-service
helm install accounts-service . --namespace dev --force
helm upgrade accounts-service . --namespace dev --force
kubectl rollout restart deployment accounts-service-accounts -n dev
kubectl get all -n dev
kubectl logs -n dev accounts-service-accounts-7484d59bdc-jsj9c



CASH-SERVICE
cd cash-service
mvn clean package -DskipTests
cd ..
eval $(minikube docker-env)
docker build -t cash-service:latest -f cash-service/Dockerfile cash-service
docker images | grep cash-service
cd helm/bankapp/charts/cash-service
helm install cash-service . --namespace dev --force
kubectl get all -n dev
helm upgrade cash-service . --namespace dev --force
kubectl rollout restart deployment cash-service -n dev



EXCHANGE-GENERATOR-SERVICE
cd exchange-generator-service
mvn clean package -DskipTests
cd ..
eval $(minikube docker-env)
docker build -t exchange-generator-service:latest -f exchange-generator-service/Dockerfile exchange-generator-service
docker images | grep exchange-generator-service
cd helm/bankapp/charts/exchange-generator-service
helm install exchange-generator-service . --namespace dev --force
kubectl get all -n dev
kubectl get secret keycloak-tls-secret -n dev -o yaml
helm upgrade exchange-generator-service . --namespace dev --force
kubectl rollout restart deployment exchange-generator-service -n dev


EXCHANGE-SERVICE
cd exchange-service
mvn clean package -DskipTests
cd ..
eval $(minikube docker-env)
docker build -t exchange-service:latest -f exchange-service/Dockerfile exchange-service
docker images | grep exchange-service
cd helm/bankapp/charts/exchange-service
helm install exchange-service . --namespace dev --force
kubectl get all -n dev
helm upgrade exchange-service . --namespace dev --force
kubectl rollout restart deployment exchange-service -n dev


NOTIFICATIONS-SERVICE
cd notifications-service
mvn clean package -DskipTests
cd ..
eval $(minikube docker-env)
docker build -t notifications-service:latest -f notifications-service/Dockerfile notifications-service
docker images | grep notifications-service
cd helm/bankapp/charts/notifications-service
helm install notifications-service . --namespace dev --force
kubectl get all -n dev
helm upgrade notifications-service . --namespace dev --force
kubectl rollout restart deployment notifications-service -n dev


TRANSFER-SERVICE
cd transfer-service
mvn clean package -DskipTests
cd ..
eval $(minikube docker-env)
docker build -t transfer-service:latest -f transfer-service/Dockerfile transfer-service
docker images | grep transfer-service
cd helm/bankapp/charts/transfer-service
helm install transfer-service . --namespace dev --force
kubectl get all -n dev
helm upgrade transfer-service . --namespace dev --force
kubectl rollout restart deployment transfer-service -n dev

kubectl port-forward -n dev svc/front-ui 8081:8080
kubectl port-forward -n dev svc/keycloak 8080:80

[//]: # (kubectl port-forward -n dev svc/keycloak 8080:8080)

curl http://notifications-service:8080/api/notifications/ping

 KEYCLOAK 
 cd helm/bankapp/charts/keycloak
 helm install keycloak . --namespace dev --force
 helm upgrade keycloak . --namespace dev --force
kubectl delete deployment keycloak -n dev
 kubectl get secret keycloak-tls-secret -n dev -o yaml
 kubectl port-forward -n dev svc/keycloak 8080:80
 kubectl logs -n dev





kubectl logs -n dev blocker-service-5b67b5cb88-dq88w
nano ~/.docker/config.json


kubectl get rs -n dev --no-headers | awk '$2=="0" {print $1}' | xargs -r kubectl delete rs -n dev
