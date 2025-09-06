DROP SCHEMA IF EXISTS accounts_service CASCADE;
DROP SCHEMA IF EXISTS cash_service CASCADE;
DROP SCHEMA IF EXISTS transfer_service CASCADE;
DROP SCHEMA IF EXISTS exchange_service CASCADE;
DROP SCHEMA IF EXISTS exchange_generator_service CASCADE;
DROP SCHEMA IF EXISTS blocker_service CASCADE;
DROP SCHEMA IF EXISTS notifications_service CASCADE;

CREATE SCHEMA accounts_service;
CREATE SCHEMA cash_service;
CREATE SCHEMA transfer_service;
CREATE SCHEMA exchange_service;
CREATE SCHEMA exchange_generator_service;
CREATE SCHEMA blocker_service;
CREATE SCHEMA notifications_service;