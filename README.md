http://localhost:8761/
http://localhost:8082/

docker exec -it postgres-bank psql -U bankuser -d bankdb
\dn
SELECT * FROM accounts_service.accounts;
SELECT * FROM accounts_service.users;

TRUNCATE TABLE accounts_service.accounts, accounts_service.users RESTART IDENTITY CASCADE;

docker compose up -d postgres-keycloak
docker compose up -d keycloak
docker compose up -d postgres
docker compose up -d config-server
docker compose up -d discovery-server
docker compose up -d accounts-service cash-service transfer-service exchange-service blocker-service notifications-service exchange-generator-service
docker compose up -d gateway
docker compose up -d front-ui
docker compose ps

