package integration;

import com.github.maximslepukhin.client.*;
import com.github.maximslepukhin.model.dto.*;
import com.github.maximslepukhin.model.entity.TransferEntity;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.model.enums.TransferStatus;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.repository.TransferRepository;
import com.github.maximslepukhin.service.TransferService;
import config.TestOAuth2Config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = com.github.maximslepukhin.TransferServiceApplication.class)
@ActiveProfiles("test")
@Import(TestOAuth2Config.class)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration"
})
class TransferIntegrationTest {

    @Autowired
    private TransferService transferService;

    @MockBean
    private AccountsClient accountsClient;

    @MockBean
    private ExchangeClient exchangeClient;

    @MockBean
    private NotificationsClient notificationsClient;

    @MockBean
    private BlockerClient blockerClient;

    @MockBean
    private TransferRepository transferRepository;

    @Test
    void contextLoads() {
        assertThat(transferService).isNotNull();
    }

    @Test
    void shouldPerformSuccessfulTransfer_withCurrencyConversion() {
        TransferRequest request = new TransferRequest();
        request.setFromLogin("alice");
        request.setToLogin("bob");
        request.setFromCurrency(Currency.USD);
        request.setToCurrency(Currency.RUB);
        request.setAmount(BigDecimal.valueOf(100));

        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies("alice")).thenReturn(List.of("USD"));
        when(accountsClient.getCurrencies("bob")).thenReturn(List.of("RUB", "USD"));
        when(exchangeClient.convert(any())).thenReturn(
                new ConvertResponse(BigDecimal.valueOf(100), Currency.USD, Currency.RUB, BigDecimal.valueOf(9500))
        );

        TransferEntity saved = TransferEntity.builder()
                .id(UUID.randomUUID())
                .status(TransferStatus.SUCCESS)
                .credited(BigDecimal.valueOf(9500))
                .build();

        when(transferRepository.save(any())).thenReturn(saved);

        TransferResponse response = transferService.transfer(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(TransferStatus.SUCCESS);
        assertThat(response.getCredited()).isEqualTo(BigDecimal.valueOf(9500));
        assertThat(response.getCurrencyTo()).isEqualTo("RUB");

        verify(accountsClient).debit("alice", "USD", BigDecimal.valueOf(100));
        verify(accountsClient).credit("bob", "RUB", BigDecimal.valueOf(9500));
        verify(notificationsClient).notify(any());
        verify(transferRepository).save(any());
    }

    @Test
    void shouldRespectBlockerMaintenancePeriod() {
        TransferRequest request = new TransferRequest();
        request.setFromLogin("alice");
        request.setToLogin("bob");
        request.setFromCurrency(Currency.USD);
        request.setToCurrency(Currency.USD);
        request.setAmount(BigDecimal.valueOf(100));

        when(blockerClient.check(any())).thenReturn(new BlockerStatus(true, "Maintenance window"));

        TransferResponse response;
        try {
            transferService.transfer(request);
            response = null; // должен выбросить исключение
        } catch (Exception e) {
            response = null;
            assertThat(e.getMessage()).contains("Maintenance");
        }

        // then
        verify(blockerClient, atLeastOnce()).check(any());
        verifyNoInteractions(accountsClient, exchangeClient, notificationsClient, transferRepository);
    }
}