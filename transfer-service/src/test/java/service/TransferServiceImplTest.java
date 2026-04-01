package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.client.ExchangeClient;
import com.github.maximslepukhin.exception.CreditFailedAfterDebitException;
import com.github.maximslepukhin.exception.MissingAccountException;
import com.github.maximslepukhin.exception.TransferBlockedException;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.TransferRequest;
import com.github.maximslepukhin.model.dto.TransferResponse;
import com.github.maximslepukhin.model.entity.TransferEntity;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.model.enums.TransferStatus;
import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.repository.OutboxEventRepository;
import com.github.maximslepukhin.repository.TransferRepository;
import com.github.maximslepukhin.service.TransferServiceImpl;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransferServiceImplTest {

    @Mock private AccountsClient accountsClient;
    @Mock private ExchangeClient exchangeClient;
    @Mock private BlockerClient blockerClient;
    @Mock private TransferRepository transferRepository;
    @Mock private OutboxEventRepository outboxEventRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    private TransferServiceImpl transferService;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transferService = new TransferServiceImpl(
                accountsClient, exchangeClient, blockerClient,
                transferRepository, outboxEventRepository,
                objectMapper, meterRegistry
        );
        transferService.initMetrics();

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

        assertThatThrownBy(() -> transferService.transfer(request, UUID.randomUUID()))
                .isInstanceOf(TransferBlockedException.class)
                .hasMessageContaining("Перевод самому себе");
    }

    @Test
    void shouldBlockTransferWhenBlockerReturnsBlocked() {
        when(blockerClient.check(any()))
                .thenReturn(new BlockerStatus(true, "Maintenance"));

        assertThatThrownBy(() -> transferService.transfer(request, UUID.randomUUID()))
                .isInstanceOf(TransferBlockedException.class)
                .hasMessageContaining("Maintenance");

        verify(blockerClient).check(any());
        verifyNoInteractions(accountsClient, exchangeClient, transferRepository);
    }

    @Test
    void shouldFailIfSenderHasNoCurrencyAccount() {
        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies("user1")).thenReturn(List.of("RUB"));
        when(accountsClient.getCurrencies("user2")).thenReturn(List.of("USD", "RUB"));

        assertThatThrownBy(() -> transferService.transfer(request, UUID.randomUUID()))
                .isInstanceOf(MissingAccountException.class)
                .hasMessageContaining("У отправителя");
    }

    @Test
    void shouldFailIfRecipientHasNoCurrencyAccount() {
        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies("user1")).thenReturn(List.of("USD"));
        when(accountsClient.getCurrencies("user2")).thenReturn(List.of("RUB"));

        request.setToCurrency(Currency.USD);

        assertThatThrownBy(() -> transferService.transfer(request, UUID.randomUUID()))
                .isInstanceOf(MissingAccountException.class)
                .hasMessageContaining("У получателя");
    }

    @Test
    void shouldConvertCurrencyAndCompleteTransfer() {
        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies(anyString())).thenReturn(List.of("USD", "RUB"));
        when(exchangeClient.convert(any())).thenReturn(new ConvertResponse(
                BigDecimal.valueOf(100), Currency.USD, Currency.RUB, BigDecimal.valueOf(9500)));
        when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UUID key = UUID.randomUUID();
        TransferResponse response = transferService.transfer(request, key);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.SUCCESS);
        assertThat(response.getCredited()).isEqualTo(BigDecimal.valueOf(9500));
        assertThat(response.getDebited()).isEqualTo(BigDecimal.valueOf(100));

        verify(accountsClient).debit(eq("user1"), eq("USD"), eq(BigDecimal.valueOf(100)), any(UUID.class));
        verify(accountsClient).credit(eq("user2"), eq("RUB"), eq(BigDecimal.valueOf(9500)), any(UUID.class));
        verify(transferRepository).save(any(TransferEntity.class));
        verify(outboxEventRepository).save(any());
    }

    @Test
    void shouldSkipConversionWhenCurrenciesAreSame() {
        request.setToCurrency(Currency.USD);

        when(blockerClient.check(any())).thenReturn(new BlockerStatus(false, ""));
        when(accountsClient.getCurrencies(anyString())).thenReturn(List.of("USD"));
        when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(outboxEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = transferService.transfer(request, UUID.randomUUID());

        assertThat(response.getCredited()).isEqualTo(BigDecimal.valueOf(100));
        verifyNoInteractions(exchangeClient);
        verify(outboxEventRepository).save(any());
    }

    @Test
    void shouldReturnFailedStatusFromFallbackOnUnexpectedException() {
        TransferResponse response = invokeFallback(request, UUID.randomUUID(), new RuntimeException("Timeout"));

        assertThat(response.getStatus()).isEqualTo(TransferStatus.FAILED);
        verify(transferRepository).save(argThat(e -> ((TransferEntity) e).getStatus() == TransferStatus.FAILED));
    }

    @Test
    void shouldNotApplyFallbackForBusinessExceptions() {
        assertThatThrownBy(() -> invokeFallback(request, UUID.randomUUID(), new TransferBlockedException("Blocked")))
                .isInstanceOf(TransferBlockedException.class);

        assertThatThrownBy(() -> invokeFallback(request, UUID.randomUUID(), new MissingAccountException("No account")))
                .isInstanceOf(MissingAccountException.class);
    }

    @Test
    void shouldCompensateWhenCreditFailsAfterDebit() {
        UUID key = UUID.randomUUID();
        UUID compensateKey = UUID.nameUUIDFromBytes((key + ":compensate").getBytes());
        CreditFailedAfterDebitException creditEx = new CreditFailedAfterDebitException(
                "credit failed", "user1", "USD", BigDecimal.valueOf(100), compensateKey);

        when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = invokeFallback(request, key, creditEx);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.COMPENSATED);
        assertThat(response.getMessage()).contains("возвращены");
        verify(accountsClient).compensate("user1", "USD", BigDecimal.valueOf(100), compensateKey);
        verify(transferRepository).save(argThat(e -> ((TransferEntity) e).getStatus() == TransferStatus.COMPENSATED));
    }

    @Test
    void shouldReturnCompensationFailedWhenCompensateThrows() {
        UUID key = UUID.randomUUID();
        UUID compensateKey = UUID.nameUUIDFromBytes((key + ":compensate").getBytes());
        CreditFailedAfterDebitException creditEx = new CreditFailedAfterDebitException(
                "credit failed", "user1", "USD", BigDecimal.valueOf(100), compensateKey);

        doThrow(new RuntimeException("accounts-service недоступен"))
                .when(accountsClient).compensate(any(), any(), any(), any());
        when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferResponse response = invokeFallback(request, key, creditEx);

        assertThat(response.getStatus()).isEqualTo(TransferStatus.COMPENSATION_FAILED);
        assertThat(response.getMessage()).contains("РУЧНОЕ ВМЕШАТЕЛЬСТВО");
        verify(transferRepository).save(argThat(e -> ((TransferEntity) e).getStatus() == TransferStatus.COMPENSATION_FAILED));
    }

    private TransferResponse invokeFallback(TransferRequest req, UUID idempotencyKey, Throwable ex) {
        try {
            var m = TransferServiceImpl.class.getDeclaredMethod(
                    "fallbackTransfer", TransferRequest.class, UUID.class, Throwable.class);
            m.setAccessible(true);
            return (TransferResponse) m.invoke(transferService, req, idempotencyKey, ex);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(e);
        }
    }
}
