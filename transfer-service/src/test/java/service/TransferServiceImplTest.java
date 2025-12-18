//package service;
//
//import com.github.maximslepukhin.client.AccountsClient;
//import com.github.maximslepukhin.client.BlockerClient;
//import com.github.maximslepukhin.client.ExchangeClient;
//import com.github.maximslepukhin.client.NotificationsClient;
//import com.github.maximslepukhin.exception.TransferBlockedException;
//import com.github.maximslepukhin.model.dto.ConvertResponse;
//import com.github.maximslepukhin.model.dto.TransferRequest;
//import com.github.maximslepukhin.model.dto.TransferResponse;
//import com.github.maximslepukhin.model.entity.TransferEntity;
//import com.github.maximslepukhin.model.enums.Currency;
//import com.github.maximslepukhin.model.enums.TransferStatus;
//import com.github.maximslepukhin.model.record.BlockerStatus;
//import com.github.maximslepukhin.repository.TransferRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import com.github.maximslepukhin.service.TransferServiceImpl;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//class TransferServiceImplTest {
//
//    @Mock
//    private AccountsClient accountsClient;
//    @Mock
//    private ExchangeClient exchangeClient;
//    @Mock
//    private NotificationsClient notificationsClient;
//    @Mock
//    private BlockerClient blockerClient;
//    @Mock
//    private TransferRepository transferRepository;
//
//    @InjectMocks
//    private TransferServiceImpl transferService;
//
//    private TransferRequest request;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        request = new TransferRequest();
//        request.setFromLogin("alice");
//        request.setToLogin("bob");
//        request.setFromCurrency(Currency.USD);
//        request.setToCurrency(Currency.RUB);
//        request.setAmount(BigDecimal.valueOf(100));
//    }
//
//    @Test
//    void shouldThrowException_whenTransferToSelfInSameCurrency() {
//        request.setToLogin("alice");
//        request.setToCurrency(Currency.USD);
//
//        assertThatThrownBy(() -> transferService.transfer(request))
//                .isInstanceOf(TransferBlockedException.class)
//                .hasMessageContaining("Перевод самому себе");
//    }
//
//    @Test
//    void shouldThrowException_whenBlockedByBlockerService() {
//        when(blockerClient.check(any())).thenReturn(new BlockerStatus(true, "System maintenance"));
//
//        assertThatThrownBy(() -> transferService.transfer(request))
//                .isInstanceOf(TransferBlockedException.class)
//                .hasMessageContaining("System maintenance");
//
//        verify(blockerClient).check(any());
//        verifyNoInteractions(accountsClient, exchangeClient, transferRepository);
//    }
//
//    @Test
//    void shouldThrowException_whenSenderHasNoRequiredCurrency() {
//        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
//        when(accountsClient.getCurrencies("alice")).thenReturn(List.of("RUB")); // нет USD
//        when(accountsClient.getCurrencies("bob")).thenReturn(List.of("RUB", "USD"));
//
//        assertThatThrownBy(() -> transferService.transfer(request))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("У отправителя нет счёта");
//    }
//
//    @Test
//    void shouldConvertCurrency_whenDifferentCurrencies() {
//        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
//        when(accountsClient.getCurrencies(anyString())).thenReturn(List.of("USD", "RUB"));
//        when(exchangeClient.convert(any())).thenReturn(
//                new ConvertResponse(BigDecimal.valueOf(100), Currency.USD, Currency.RUB, BigDecimal.valueOf(9500))
//        );
//
//        TransferEntity saved = TransferEntity.builder()
//                .id(UUID.randomUUID())
//                .status(TransferStatus.SUCCESS)
//                .build();
//        when(transferRepository.save(any())).thenReturn(saved);
//
//        TransferResponse response = transferService.transfer(request);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(TransferStatus.SUCCESS);
//        assertThat(response.getCredited()).isEqualTo(BigDecimal.valueOf(9500));
//
//        verify(accountsClient).debit("alice", "USD", BigDecimal.valueOf(100));
//        verify(accountsClient).credit("bob", "RUB", BigDecimal.valueOf(9500));
//        verify(notificationsClient).notify(any());
//        verify(transferRepository).save(any());
//    }
//
//    @Test
//    void shouldSkipConversion_whenSameCurrency() {
//        request.setToCurrency(Currency.USD);
//
//        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
//        when(accountsClient.getCurrencies(anyString())).thenReturn(List.of("USD"));
//        when(transferRepository.save(any())).thenReturn(mock(TransferEntity.class));
//
//        TransferResponse response = transferService.transfer(request);
//
//        assertThat(response.getCurrencyFrom()).isEqualTo("USD");
//        assertThat(response.getCurrencyTo()).isEqualTo("USD");
//        assertThat(response.getCredited()).isEqualTo(BigDecimal.valueOf(100));
//
//        verifyNoInteractions(exchangeClient);
//    }
//
//    @Test
//    void shouldHandleNotificationFailureGracefully() {
//        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
//        when(accountsClient.getCurrencies(anyString())).thenReturn(List.of("USD", "RUB"));
//        when(exchangeClient.convert(any())).thenReturn(
//                new ConvertResponse(BigDecimal.valueOf(100), Currency.USD, Currency.RUB, BigDecimal.valueOf(9500))
//        );
//
//        doThrow(new RuntimeException("Notification service unavailable"))
//                .when(notificationsClient).notify(any());
//
//        when(transferRepository.save(any())).thenReturn(mock(TransferEntity.class));
//
//        TransferResponse response = transferService.transfer(request);
//
//        assertThat(response.getStatus()).isEqualTo(TransferStatus.SUCCESS);
//        verify(notificationsClient).notify(any());
//        verify(transferRepository).save(any());
//    }
//
//    @Test
//    void shouldReturnFailedTransfer_whenFallbackTriggered() {
//        when(blockerClient.check(any())).thenThrow(new RuntimeException("Feign timeout"));
//
//        TransferResponse response = invokeFallback(request, new RuntimeException("Feign timeout"));
//
//        assertThat(response.getStatus()).isEqualTo(TransferStatus.FAILED);
//        assertThat(response.getMessage()).contains("unavailability");
//        verify(transferRepository).save(any());
//    }
//
//    private TransferResponse invokeFallback(TransferRequest req, Throwable ex) {
//        try {
//            var method = TransferServiceImpl.class.getDeclaredMethod("fallbackTransfer", TransferRequest.class, Throwable.class);
//            method.setAccessible(true);
//            return (TransferResponse) method.invoke(transferService, req, ex);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
