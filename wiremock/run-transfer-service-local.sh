#!/bin/bash
# Запускает transfer-service локально, направляя HTTP-зависимости в WireMock.
#
# Предварительно:
#   1. Запусти WireMock:  cd wiremock && docker compose up
#   2. Пробрось БД из кластера (если не поднята локально):
#        kubectl port-forward -n dev svc/accounts-db 5432:5432
#   3. Собери jar:
#        /Users/maksim/apps/apache-maven-3.9.9/bin/mvn -pl transfer-service package -DskipTests

set -e

JAR=$(ls /Users/maksim/Desktop/All/bankapp/transfer-service/target/transfer-service-*.jar 2>/dev/null | head -1)

if [ -z "$JAR" ]; then
  echo "JAR не найден. Сначала запусти: mvn -pl transfer-service package -DskipTests"
  exit 1
fi

echo "Запускаю transfer-service -> accounts-mock:8091, blocker-mock:8092, exchange-mock:8093"

ACCOUNTS_SERVICE_URL=http://localhost:8091 \
BLOCKER_SERVICE_URL=http://localhost:8092 \
EXCHANGE_SERVICE_URL=http://localhost:8093 \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/accounts_db \
SPRING_DATASOURCE_USERNAME=admin \
SPRING_DATASOURCE_PASSWORD=password \
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=http://localhost:8080/realms/bank-realm/protocol/openid-connect/certs \
KEYCLOAK_CLIENTSECRET=RMU6Vb96Ay0fWnLJ6WEBiyt8yjdjKk24 \
java -jar "$JAR" \
  --management.zipkin.tracing.export.enabled=false \
  --management.tracing.sampling.probability=0
