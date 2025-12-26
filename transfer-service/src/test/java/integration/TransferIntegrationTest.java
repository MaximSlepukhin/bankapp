package integration;

import com.github.maximslepukhin.TransferServiceApplication;
import com.github.maximslepukhin.client.*;
import com.github.maximslepukhin.config.kafka.NotificationKafkaProducer;
import com.github.maximslepukhin.model.dto.*;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.model.enums.TransferStatus;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.repository.TransferRepository;
import com.github.maximslepukhin.service.TransferService;
import com.github.maximslepukhin.service.TransferServiceImpl;
import config.TestOAuth2Config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = TransferServiceApplication.class,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        }
)
@ActiveProfiles("test")
@Import({TransferServiceImpl.class, TestOAuth2Config.class})
class TransferIntegrationTest {

    @Autowired
    private TransferService transferService;

    @MockBean
    private AccountsClient accountsClient;
    @MockBean
    private ExchangeClient exchangeClient;
    @MockBean
    private BlockerClient blockerClient;
    @MockBean
    private TransferRepository transferRepository;
    @MockBean
    private NotificationKafkaProducer notificationKafkaProducer;

    @Test
    void contextLoads() {
        assertThat(transferService).isNotNull();
    }

    @Test
    void shouldPerformSuccessfulTransfer() {
        TransferRequest request = new TransferRequest();
        request.setFromLogin("alice");
        request.setToLogin("bob");
        request.setFromCurrency(Currency.USD);
        request.setToCurrency(Currency.RUB);
        request.setAmount(BigDecimal.valueOf(100));

        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies(anyString())).thenReturn(List.of("USD", "RUB"));
        when(exchangeClient.convert(any())).thenReturn(new ConvertResponse(
                BigDecimal.valueOf(100),
                Currency.USD,
                Currency.RUB,
                BigDecimal.valueOf(9500)
        ));

        when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = transferService.transfer(request);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.SUCCESS);
        verify(notificationKafkaProducer).send(any());
    }
}
