package service;

import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.client.ExchangeClient;
import com.github.maximslepukhin.config.security.kafka.NotificationKafkaProducer;
import com.github.maximslepukhin.exception.TransferBlockedException;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.TransferRequest;
import com.github.maximslepukhin.model.dto.TransferResponse;
import com.github.maximslepukhin.model.entity.TransferEntity;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.model.enums.TransferStatus;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import com.github.maximslepukhin.service.TransferServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransferServiceImplTest {

    @Mock
    private AccountsClient accountsClient;
    @Mock
    private ExchangeClient exchangeClient;
    @Mock
    private BlockerClient blockerClient;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private NotificationKafkaProducer notificationKafkaProducer;

    @InjectMocks
    private TransferServiceImpl transferService;

    private TransferRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new TransferRequest();
        request.setFromLogin("user1");
        request.setToLogin("user2");
        request.setFromCurrency(Currency.USD);
        request.setToCurrency(Currency.RUB);
        request.setAmount(BigDecimal.valueOf(100));
    }

    @Test
    void shouldBlockTransferToSelfInSameCurrency() {
        request.setToLogin("user1");
        request.setToCurrency(Currency.USD);

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(TransferBlockedException.class)
                .hasMessageContaining("Перевод самому себе");
    }

    @Test
    void shouldBlockTransferWhenBlockerReturnsBlocked() {
        when(blockerClient.check(any()))
                .thenReturn(new BlockerStatus(true, "Maintenance"));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(TransferBlockedException.class)
                .hasMessageContaining("Maintenance");

        verify(blockerClient).check(any());
        verifyNoInteractions(accountsClient, exchangeClient, transferRepository, notificationKafkaProducer);
    }

    @Test
    void shouldFailIfSenderHasNoCurrencyAccount() {
        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies("user1")).thenReturn(List.of("RUB"));
        when(accountsClient.getCurrencies("user2")).thenReturn(List.of("USD", "RUB"));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("У отправителя нет счёта");
    }

    @Test
    void shouldConvertCurrencyAndCompleteTransfer() {
        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies(anyString()))
                .thenReturn(List.of("USD", "RUB"));

        when(exchangeClient.convert(any()))
                .thenReturn(new ConvertResponse(
                        BigDecimal.valueOf(100),
                        Currency.USD,
                        Currency.RUB,
                        BigDecimal.valueOf(9500)
                ));

        when(transferRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = transferService.transfer(request);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.SUCCESS);
        assertThat(response.getCredited()).isEqualTo(BigDecimal.valueOf(9500));

        verify(accountsClient).debit("user1", "USD", BigDecimal.valueOf(100));
        verify(accountsClient).credit("user2", "RUB", BigDecimal.valueOf(9500));
        verify(notificationKafkaProducer).send(any());
        verify(transferRepository).save(any(TransferEntity.class));
    }

    @Test
    void shouldSkipConversionWhenCurrenciesAreSame() {
        request.setToCurrency(Currency.USD);

        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies(anyString()))
                .thenReturn(List.of("USD"));

        when(transferRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = transferService.transfer(request);

        assertThat(response.getCredited()).isEqualTo(BigDecimal.valueOf(100));
        verifyNoInteractions(exchangeClient);
        verify(notificationKafkaProducer).send(any());
    }

    @Test
    void shouldUseFallbackOnUnexpectedException() {
        when(blockerClient.check(any()))
                .thenThrow(new RuntimeException("Timeout"));

        TransferResponse response = invokeFallback(request, new RuntimeException("Timeout"));

        assertThat(response.getStatus()).isEqualTo(TransferStatus.FAILED);
        verify(transferRepository).save(any());
    }

    private TransferResponse invokeFallback(TransferRequest req, Throwable ex) {
        try {
            var m = TransferServiceImpl.class
                    .getDeclaredMethod("fallbackTransfer", TransferRequest.class, Throwable.class);
            m.setAccessible(true);
            return (TransferResponse) m.invoke(transferService, req, ex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}