package integration;

import com.github.maximslepukhin.CashServiceApplication;
import com.github.maximslepukhin.config.kafka.KafkaNotificationProducer;
import com.github.maximslepukhin.exception.InsufficientFundsException;
import com.github.maximslepukhin.exception.OperationBlockedException;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.CashService;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Интеграционный тест с WireMock.
 *
 * Чем отличается от CashServiceIntegrationTest (где AccountsClient — @MockBean):
 *  - Здесь поднимается настоящий HTTP-сервер (WireMock).
 *  - RestTemplate в AccountsClient/BlockerClient делает реальные HTTP-запросы к нему.
 *  - Можно проверять URL, заголовки и тело запроса — а не только то, что метод был вызван.
 *
 * Порядок инициализации:
 *  1. static-блок запускает WireMockServer с динамическим портом
 *  2. @DynamicPropertySource читает порт и переопределяет URL до старта Spring-контекста
 *  3. Spring поднимает контекст — RestTemplate уже смотрит на WireMock
 */
@SpringBootTest(
        classes = CashServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.kafka.bootstrap-servers=localhost:9092",
                "management.zipkin.tracing.export.enabled=false",
                "management.tracing.sampling.probability=0"
        }
)
@ActiveProfiles("test")
@Testcontainers
class CashServiceWireMockTest {

    // -----------------------------------------------------------------------
    // WireMock — два сервера: один для accounts-service, другой для blocker
    // static-поля инициализируются раньше, чем @DynamicPropertySource, поэтому
    // порты уже известны к моменту старта Spring-контекста
    // -----------------------------------------------------------------------

    private static final WireMockServer accountsMock =
            new WireMockServer(wireMockConfig().dynamicPort());

    private static final WireMockServer blockerMock =
            new WireMockServer(wireMockConfig().dynamicPort());

    static {
        accountsMock.start();
        blockerMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        accountsMock.stop();
        blockerMock.stop();
    }

    /**
     * Переопределяем URL внешних сервисов до старта Spring-контекста.
     * Вместо http://localhost:8081 и :8087 RestTemplate пойдёт в WireMock.
     */
    @DynamicPropertySource
    static void wireMockProperties(DynamicPropertyRegistry registry) {
        registry.add("ACCOUNTS_SERVICE_URL", () -> "http://localhost:" + accountsMock.port());
        registry.add("BLOCKER_SERVICE_URL",  () -> "http://localhost:" + blockerMock.port());
    }

    // -----------------------------------------------------------------------
    // Testcontainers — реальная БД нужна для IdempotencyService
    // -----------------------------------------------------------------------

    @Container
    @ServiceConnection
    @SuppressWarnings("resource") // Testcontainers управляет lifecycle через @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withInitScript("init-schema.sql");

    // -----------------------------------------------------------------------
    // Инфраструктурные бины, не относящиеся к тестируемой логике
    // -----------------------------------------------------------------------

    @MockBean OAuth2AuthorizedClientManager authorizedClientManager;
    @MockBean ClientRegistrationRepository  clientRegistrationRepository;
    @MockBean JwtDecoder                    jwtDecoder;
    @MockBean Tracer                        tracer;
    @MockBean KafkaNotificationProducer     kafkaProducer;

    @Autowired
    CashService cashService;

    /** Сбрасываем стабы перед каждым тестом, чтобы они не влияли друг на друга. */
    @BeforeEach
    void resetStubs() {
        accountsMock.resetAll();
        blockerMock.resetAll();
    }

    // -----------------------------------------------------------------------
    // Тесты: deposit
    // -----------------------------------------------------------------------

    @Test
    void deposit_shouldSucceed_whenNotBlocked() {
        blockerMock.stubFor(post(urlEqualTo("/api/v1/blocker/check"))
                .willReturn(okJson("{\"blocked\":false,\"message\":\"\"}")));

        accountsMock.stubFor(post(urlMatching("/api/v1/accounts/.*"))
                .willReturn(ok()));

        UUID key = UUID.randomUUID();
        CashOperationDto dto = new CashOperationDto(Currency.RUB, BigDecimal.valueOf(1000), "alice");

        assertThatCode(() -> cashService.deposit(dto, key)).doesNotThrowAnyException();

        // WireMock позволяет убедиться, что запрос ушёл именно на нужный URL
        accountsMock.verify(1, postRequestedFor(urlEqualTo("/api/v1/accounts/alice/RUB")));
    }

    @Test
    void deposit_shouldForwardIdempotencyKeyHeader() {
        // Ключевое отличие от unit-теста: мы проверяем сам HTTP-запрос,
        // а не просто вызов метода Java — именно здесь WireMock незаменим
        blockerMock.stubFor(post(urlEqualTo("/api/v1/blocker/check"))
                .willReturn(okJson("{\"blocked\":false,\"message\":\"\"}")));

        accountsMock.stubFor(post(urlMatching("/api/v1/accounts/.*"))
                .willReturn(ok()));

        UUID key = UUID.randomUUID();
        CashOperationDto dto = new CashOperationDto(Currency.USD, BigDecimal.valueOf(500), "alice");

        cashService.deposit(dto, key);

        accountsMock.verify(postRequestedFor(urlEqualTo("/api/v1/accounts/alice/USD"))
                .withHeader("X-Idempotency-Key", equalTo(key.toString())));
    }

    @Test
    void deposit_shouldThrowOperationBlockedException_whenBlockerBlocks() {
        blockerMock.stubFor(post(urlEqualTo("/api/v1/blocker/check"))
                .willReturn(okJson("{\"blocked\":true,\"message\":\"Подозрительная операция\"}")));

        UUID key = UUID.randomUUID();
        CashOperationDto dto = new CashOperationDto(Currency.RUB, BigDecimal.valueOf(100), "bob");

        assertThatThrownBy(() -> cashService.deposit(dto, key))
                .isInstanceOf(OperationBlockedException.class)
                .hasMessageContaining("Операция заблокирована");

        // accounts-service не должен получить ни одного запроса
        accountsMock.verify(0, postRequestedFor(anyUrl()));
    }

    // -----------------------------------------------------------------------
    // Тесты: withdraw
    // -----------------------------------------------------------------------

    @Test
    void withdraw_shouldSucceed_andSendNegativeAmount() {
        accountsMock.stubFor(get(urlEqualTo("/api/v1/accounts/charlie/RUB"))
                .willReturn(okJson("500.00")));

        blockerMock.stubFor(post(urlEqualTo("/api/v1/blocker/check"))
                .willReturn(okJson("{\"blocked\":false,\"message\":\"\"}")));

        accountsMock.stubFor(post(urlEqualTo("/api/v1/accounts/charlie/RUB"))
                .willReturn(ok()));

        UUID key = UUID.randomUUID();
        CashOperationDto dto = new CashOperationDto(Currency.RUB, BigDecimal.valueOf(200), "charlie");

        assertThatCode(() -> cashService.withdraw(dto, key)).doesNotThrowAnyException();

        // Снятие должно передать отрицательную сумму (-200), а не положительную
        accountsMock.verify(postRequestedFor(urlEqualTo("/api/v1/accounts/charlie/RUB"))
                .withRequestBody(equalToJson("-200")));
    }

    @Test
    void withdraw_shouldThrowInsufficientFundsException_whenBalanceTooLow() {
        // accounts-service вернул 50, пользователь хочет снять 200
        accountsMock.stubFor(get(urlEqualTo("/api/v1/accounts/dave/USD"))
                .willReturn(okJson("50.00")));

        UUID key = UUID.randomUUID();
        CashOperationDto dto = new CashOperationDto(Currency.USD, BigDecimal.valueOf(200), "dave");

        assertThatThrownBy(() -> cashService.withdraw(dto, key))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Недостаточно средств");

        // Убеждаемся, что блокировку и списание даже не проверяли
        blockerMock.verify(0, postRequestedFor(anyUrl()));
        accountsMock.verify(0, postRequestedFor(anyUrl()));
    }

    @Test
    void withdraw_shouldThrowOperationBlockedException_whenBlockerBlocks() {
        accountsMock.stubFor(get(urlEqualTo("/api/v1/accounts/eve/CNY"))
                .willReturn(okJson("1000.00")));

        blockerMock.stubFor(post(urlEqualTo("/api/v1/blocker/check"))
                .willReturn(okJson("{\"blocked\":true,\"message\":\"Заблокировано\"}")));

        UUID key = UUID.randomUUID();
        CashOperationDto dto = new CashOperationDto(Currency.CNY, BigDecimal.valueOf(100), "eve");

        assertThatThrownBy(() -> cashService.withdraw(dto, key))
                .isInstanceOf(OperationBlockedException.class);

        // Баланс был проверен, но списание не произошло
        accountsMock.verify(1, getRequestedFor(urlEqualTo("/api/v1/accounts/eve/CNY")));
        accountsMock.verify(0, postRequestedFor(anyUrl()));
    }

    @Test
    void withdraw_shouldReturn503_whenAccountsServiceIsDown() {
        // Симулируем недоступность accounts-service: WireMock вернёт 503
        accountsMock.stubFor(get(urlMatching("/api/v1/accounts/.*"))
                .willReturn(serviceUnavailable()));

        UUID key = UUID.randomUUID();
        CashOperationDto dto = new CashOperationDto(Currency.RUB, BigDecimal.valueOf(100), "frank");

        // Circuit breaker Resilience4j в fallback-методе AccountsClient бросает RuntimeException
        assertThatThrownBy(() -> cashService.withdraw(dto, key))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("accounts-service недоступен");
    }
}
