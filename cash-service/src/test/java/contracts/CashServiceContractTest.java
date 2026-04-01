package contracts;

import com.github.maximslepukhin.CashServiceApplication;
import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.config.kafka.KafkaNotificationProducer;
import com.github.maximslepukhin.model.enums.Currency;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CONSUMER CONTRACT TEST — Потребительская сторона контракта.
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  Как работает:                                                       │
 * │                                                                      │
 * │  1. Провайдер (accounts-service) описывает контракты в YAML,        │
 * │     прогоняет их через spring-cloud-contract-maven-plugin и          │
 * │     публикует stubs-JAR в локальный ~/.m2 командой:                 │
 * │       mvn -pl accounts-service install                               │
 * │       mvn -pl blocker-service  install                               │
 * │                                                                      │
 * │  2. @AutoConfigureStubRunner читает stubs-JAR из ~/.m2 и поднимает  │
 * │     WireMock на фиксированных портах 9091 / 9092.                   │
 * │                                                                      │
 * │  3. AccountsClient и BlockerClient (RestTemplate) настроены на эти  │
 * │     порты через ACCOUNTS_SERVICE_URL / BLOCKER_SERVICE_URL.         │
 * │                                                                      │
 * │  4. Тест вызывает клиентов и проверяет, что они правильно           │
 * │     разобрали HTTP-ответ, предписанный контрактом.                  │
 * │                                                                      │
 * │  Ключевая гарантия: если провайдер изменит API (другой статус,      │
 * │  другой формат ответа) — его собственные контрактные тесты          │
 * │  упадут при mvn install. Потребитель всегда получает актуальные     │
 * │  stubs, отражающие реальное поведение провайдера.                   │
 * └──────────────────────────────────────────────────────────────────────┘
 */
@SpringBootTest(
        classes = CashServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                // Направляем клиентов на порты, где stub runner поднял WireMock
                "ACCOUNTS_SERVICE_URL=http://localhost:9091",
                "BLOCKER_SERVICE_URL=http://localhost:9099",
                "spring.kafka.bootstrap-servers=localhost:9092",
                "management.zipkin.tracing.export.enabled=false",
                "management.tracing.sampling.probability=0"
        }
)
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureStubRunner(
        /*
         * Формат ids: "groupId:artifactId:version:classifier:port"
         *   +:stubs  — берёт последнюю доступную версию stubs-JAR
         *   9091     — фиксированный порт WireMock (должен совпадать с ACCOUNTS_SERVICE_URL)
         *
         * Stub runner ищет JAR в ~/.m2/repository/com/example/bank/accounts-service/...
         * Установить можно командой: mvn -pl accounts-service install
         */
        ids = {
                "com.example.bank:accounts-service:+:stubs:9091",
                "com.example.bank:blocker-service:+:stubs:9099"
        },
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class CashServiceContractTest {

    // Реальная БД нужна, потому что IdempotencyService использует JPA
    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withInitScript("init-schema.sql");

    // Бины, которые не нужны для этого теста — мокируем, чтобы не поднимать
    // реальный Keycloak и Kafka
    @MockBean OAuth2AuthorizedClientManager authorizedClientManager;
    @MockBean ClientRegistrationRepository  clientRegistrationRepository;
    @MockBean JwtDecoder                    jwtDecoder;
    @MockBean Tracer                        tracer;
    @MockBean KafkaNotificationProducer     kafkaProducer;

    // Тестируем именно клиенты — они делают HTTP-запросы к stub runner WireMock
    @Autowired AccountsClient accountsClient;
    @Autowired BlockerClient  blockerClient;

    // -----------------------------------------------------------------------
    // Тесты против контрактов accounts-service
    // -----------------------------------------------------------------------

    @Test
    void accountsClient_shouldReturnBalance_forAliceUsd() {
        /*
         * Stub runner поднял WireMock с маппингом из get_balance.yaml:
         *   GET /api/v1/accounts/alice/USD → 200, body: 1000
         *
         * AccountsClient.getBalance() делает RestTemplate.getForObject() к этому WireMock.
         * Здесь проверяем, что клиент правильно десериализовал ответ в BigDecimal.
         */
        BigDecimal balance = accountsClient.getBalance("alice", Currency.USD);

        assertThat(balance).isEqualByComparingTo(new BigDecimal("1000"));
    }

    // -----------------------------------------------------------------------
    // Тесты против контрактов blocker-service
    // -----------------------------------------------------------------------

    @Test
    void blockerClient_shouldReturnNotBlocked_forAlice() {
        /*
         * Stub runner WireMock: check_not_blocked.yaml
         *   POST /api/v1/blocker/check body содержит login=alice → 200, {blocked: false}
         *
         * BlockerClient отправляет {login, currency, amount}; стаб матчит только по $.login,
         * поэтому лишние поля не мешают.
         */
        boolean blocked = blockerClient.isBlocked("alice", Currency.USD, BigDecimal.valueOf(500));

        assertThat(blocked).isFalse();
    }

    @Test
    void blockerClient_shouldReturnBlocked_forBlockedUser() {
        /*
         * Stub runner WireMock: check_blocked.yaml
         *   POST /api/v1/blocker/check body содержит login=blocked_user → 403, {blocked: true}
         *
         * BlockerClient.isBlocked() проверяет поле blocked в ответе.
         * 403 сам по себе не вызывает исключение (RestTemplate получает тело),
         * поэтому клиент разбирает JSON и возвращает true.
         */
        boolean blocked = blockerClient.isBlocked("blocked_user", Currency.RUB, BigDecimal.valueOf(100));

        assertThat(blocked).isTrue();
    }
}
