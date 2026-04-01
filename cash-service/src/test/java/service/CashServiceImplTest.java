package service;

import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.config.kafka.KafkaNotificationProducer;
import com.github.maximslepukhin.exception.InsufficientFundsException;
import com.github.maximslepukhin.exception.OperationBlockedException;
import com.github.maximslepukhin.exception.OperationFailedException;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.CashServiceImpl;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CashServiceImplTest {

    @Mock private AccountsClient accountsClient;
    @Mock private BlockerClient blockerClient;
    @Mock private KafkaNotificationProducer kafkaProducer;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks
    private CashServiceImpl cashService;

    private CashOperationDto dto;
    private UUID key;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cashService = new CashServiceImpl(accountsClient, blockerClient, kafkaProducer, meterRegistry);
        dto = new CashOperationDto(Currency.RUB, BigDecimal.valueOf(100), "user1");
        key = UUID.randomUUID();
    }

    @Test
    void deposit_ShouldThrow_WhenBlocked() {
        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(true);

        assertThrows(OperationBlockedException.class, () -> cashService.deposit(dto, key));

        verify(accountsClient, never()).updateBalance(any(), any(), any(), any());
    }

    @Test
    void deposit_ShouldCallAccountsClient_WhenNotBlocked() {
        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(false);

        cashService.deposit(dto, key);

        verify(accountsClient).updateBalance("user1", Currency.RUB, BigDecimal.valueOf(100), key);
        verify(kafkaProducer).send(any());
    }

    @Test
    void deposit_ShouldThrowOperationFailed_WhenAccountsFails() {
        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(false);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ошибка"))
                .when(accountsClient).updateBalance(any(), any(), any(), any());

        assertThrows(OperationFailedException.class, () -> cashService.deposit(dto, key));
    }

    @Test
    void withdraw_ShouldThrow_WhenInsufficientFunds() {
        when(accountsClient.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(50));

        assertThrows(InsufficientFundsException.class, () -> cashService.withdraw(dto, key));
    }

    @Test
    void withdraw_ShouldThrow_WhenBlocked() {
        when(accountsClient.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(200));
        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(true);

        assertThrows(OperationBlockedException.class, () -> cashService.withdraw(dto, key));
    }

    @Test
    void withdraw_ShouldCallAccountsClient_WhenOk() {
        when(accountsClient.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(200));
        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(false);

        cashService.withdraw(dto, key);

        verify(accountsClient).updateBalance("user1", Currency.RUB, BigDecimal.valueOf(100).negate(), key);
        verify(kafkaProducer).send(any());
    }
}
