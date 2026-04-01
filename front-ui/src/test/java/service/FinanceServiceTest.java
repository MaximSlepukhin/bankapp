package service;

import com.github.maximslepukhin.client.CashClient;
import com.github.maximslepukhin.client.TransferClient;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.model.dto.TransferRequestDto;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.FinanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanceServiceTest {

    @Mock
    private CashClient cashClient;
    @Mock
    private TransferClient transferClient;

    @InjectMocks
    private FinanceService financeService;

    private UUID key;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        key = UUID.randomUUID();
    }

    @Test
    void deposit_ShouldCallCashClient() {
        financeService.deposit("john", Currency.USD, BigDecimal.valueOf(100), key);
        verify(cashClient, times(1)).deposit(any(CashOperationDto.class), eq(key));
    }

    @Test
    void withdraw_ShouldCallCashClient() {
        financeService.withdraw("john", Currency.RUB, BigDecimal.valueOf(50), key);
        verify(cashClient, times(1)).withdraw(any(CashOperationDto.class), eq(key));
    }

    @Test
    void transfer_ShouldCallTransferClient() {
        financeService.transfer("john", "mary", Currency.USD, Currency.RUB, BigDecimal.valueOf(10), key);
        verify(transferClient, times(1)).transfer(any(TransferRequestDto.class), eq(key));
    }

    @Test
    void transfer_ShouldThrowException_WhenAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                financeService.transfer("john", "mary", Currency.USD, Currency.RUB, BigDecimal.valueOf(-5), key)
        );
    }

    @Test
    void deposit_ShouldThrowRuntimeException_WhenClientErrorOccurs() {
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"))
                .when(cashClient).deposit(any(CashOperationDto.class), any(UUID.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                financeService.deposit("john", Currency.USD, BigDecimal.TEN, key)
        );
        assertTrue(ex.getMessage().contains("Ошибка"));
    }

    @Test
    void cashOperation_PUT_ShouldCallDeposit() {
        financeService.cashOperation("john", Currency.USD, BigDecimal.valueOf(100), "PUT", key);
        verify(cashClient).deposit(any(CashOperationDto.class), eq(key));
    }

    @Test
    void cashOperation_GET_ShouldCallWithdraw() {
        financeService.cashOperation("john", Currency.USD, BigDecimal.valueOf(50), "GET", key);
        verify(cashClient).withdraw(any(CashOperationDto.class), eq(key));
    }

    @Test
    void cashOperation_ShouldThrow_WhenAmountIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                financeService.cashOperation("john", Currency.USD, BigDecimal.ZERO, "PUT", key)
        );
    }
}
