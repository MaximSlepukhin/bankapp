package integration;

import com.github.maximslepukhin.CashServiceApplication;
import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.config.kafka.KafkaNotificationProducer;
import com.github.maximslepukhin.idempotency.IdempotencyService;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.model.entity.IdempotencyKey;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.model.enums.IdempotencyStatus;
import com.github.maximslepukhin.service.CashService;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = CashServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "management.zipkin.tracing.export.enabled=false",
                "management.tracing.sampling.probability=0",
                "ACCOUNTS_SERVICE_URL=http://localhost:8081",
                "BLOCKER_SERVICE_URL=http://localhost:8087"
        }
)
@ActiveProfiles("test")
@Testcontainers
class CashServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withInitScript("init-schema.sql");

    @MockBean
    private AccountsClient accountsClient;

    @MockBean
    private BlockerClient blockerClient;

    @MockBean
    private KafkaNotificationProducer kafkaProducer;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @MockBean
    private Tracer tracer;

    @Autowired
    private CashService cashService;

    @Autowired
    private IdempotencyService idempotencyService;

    @Test
    void contextLoads() {
        assertThat(cashService).isNotNull();
        assertThat(idempotencyService).isNotNull();
    }

    @Test
    void idempotency_reserve_shouldPersistKey() {
        UUID key = UUID.randomUUID();

        idempotencyService.reserve(key, "deposit");

        Optional<IdempotencyKey> found = idempotencyService.find(key);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(IdempotencyStatus.PROCESSING);
        assertThat(found.get().getOperation()).isEqualTo("deposit");
    }

    @Test
    void idempotency_complete_shouldUpdateStatus() {
        UUID key = UUID.randomUUID();
        idempotencyService.reserve(key, "deposit");

        idempotencyService.complete(key, 200);

        Optional<IdempotencyKey> found = idempotencyService.find(key);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
        assertThat(found.get().getHttpStatus()).isEqualTo(200);
    }

    @Test
    void idempotency_fail_shouldUpdateStatus() {
        UUID key = UUID.randomUUID();
        idempotencyService.reserve(key, "withdraw");

        idempotencyService.fail(key, 400);

        Optional<IdempotencyKey> found = idempotencyService.find(key);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(IdempotencyStatus.FAILED);
    }

    @Test
    void deposit_shouldCallAccountsClient_withIdempotencyKey() {
        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(false);

        UUID key = UUID.randomUUID();
        CashOperationDto dto = new CashOperationDto(Currency.RUB, BigDecimal.valueOf(100), "user1");

        cashService.deposit(dto, key);

        org.mockito.Mockito.verify(accountsClient)
                .updateBalance("user1", Currency.RUB, BigDecimal.valueOf(100), key);
    }
}
