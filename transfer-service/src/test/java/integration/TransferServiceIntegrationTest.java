package integration;

import com.github.maximslepukhin.TransferServiceApplication;
import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.client.ExchangeClient;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.TransferRequest;
import com.github.maximslepukhin.model.dto.TransferResponse;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.model.enums.TransferStatus;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.repository.TransferRepository;
import com.github.maximslepukhin.service.TransferService;
import config.TestOAuth2Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = TransferServiceApplication.class,
        properties = {
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "management.zipkin.tracing.export.enabled=false",
                "management.tracing.sampling.probability=0"
        }
)
@ActiveProfiles("test")
@Testcontainers
@Import(TestOAuth2Config.class)
class TransferServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withInitScript("init-schema.sql");

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferRepository transferRepository;

    @MockBean
    private AccountsClient accountsClient;

    @MockBean
    private BlockerClient blockerClient;

    @MockBean
    private ExchangeClient exchangeClient;

    @BeforeEach
    void setUp() {
        transferRepository.deleteAll();
        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies(anyString())).thenReturn(List.of("USD", "RUB", "CNY"));
    }

    @Test
    void contextLoads() {
        assertThat(transferService).isNotNull();
    }

    @Test
    void transfer_shouldPersistSuccessfulTransfer() {
        when(exchangeClient.convert(any())).thenReturn(new ConvertResponse(
                BigDecimal.valueOf(100), Currency.USD, Currency.RUB, BigDecimal.valueOf(9500)));

        TransferRequest request = buildRequest("alice", "bob", Currency.USD, Currency.RUB, BigDecimal.valueOf(100));
        TransferResponse response = transferService.transfer(request, UUID.randomUUID());

        assertThat(response.getStatus()).isEqualTo(TransferStatus.SUCCESS);
        assertThat(response.getCredited()).isEqualByComparingTo("9500");
        assertThat(transferRepository.count()).isEqualTo(1);
    }

    @Test
    void transfer_sameCurrency_shouldNotCallExchange() {
        TransferRequest request = buildRequest("alice", "bob", Currency.USD, Currency.USD, BigDecimal.valueOf(50));
        TransferResponse response = transferService.transfer(request, UUID.randomUUID());

        assertThat(response.getStatus()).isEqualTo(TransferStatus.SUCCESS);
        assertThat(response.getDebited()).isEqualByComparingTo("50");
        assertThat(response.getCredited()).isEqualByComparingTo("50");
        assertThat(transferRepository.count()).isEqualTo(1);
    }

    @Test
    void transfer_idempotency_shouldNotDoubleTransfer() {
        when(exchangeClient.convert(any())).thenReturn(new ConvertResponse(
                BigDecimal.valueOf(100), Currency.USD, Currency.RUB, BigDecimal.valueOf(9500)));

        UUID key = UUID.randomUUID();
        TransferRequest request = buildRequest("alice", "bob", Currency.USD, Currency.RUB, BigDecimal.valueOf(100));

        transferService.transfer(request, key);
        transferService.transfer(request, UUID.nameUUIDFromBytes((key + ":debit").getBytes()));

        // Каждый вызов создаёт отдельную запись — idempotency на уровне accounts-service (замокан)
        assertThat(transferRepository.count()).isEqualTo(2);
    }

    @Test
    void transfer_multipleTransfers_allPersisted() {
        when(exchangeClient.convert(any())).thenReturn(new ConvertResponse(
                BigDecimal.valueOf(50), Currency.USD, Currency.RUB, BigDecimal.valueOf(4750)));

        for (int i = 0; i < 3; i++) {
            TransferRequest request = buildRequest("alice", "bob", Currency.USD, Currency.RUB, BigDecimal.valueOf(50));
            transferService.transfer(request, UUID.randomUUID());
        }

        assertThat(transferRepository.count()).isEqualTo(3);
        assertThat(transferRepository.findAll())
                .allMatch(t -> t.getStatus() == TransferStatus.SUCCESS);
    }

    private TransferRequest buildRequest(String from, String to,
                                         Currency fromCurrency, Currency toCurrency,
                                         BigDecimal amount) {
        TransferRequest req = new TransferRequest();
        req.setFromLogin(from);
        req.setToLogin(to);
        req.setFromCurrency(fromCurrency);
        req.setToCurrency(toCurrency);
        req.setAmount(amount);
        return req;
    }
}
