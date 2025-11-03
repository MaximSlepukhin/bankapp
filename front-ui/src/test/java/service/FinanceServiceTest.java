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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FinanceServiceTest {

    @Mock
    private CashClient cashClient;
    @Mock
    private TransferClient transferClient;

    @InjectMocks
    private FinanceService financeService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deposit_ShouldCallCashClient() {
        financeService.deposit("john", Currency.USD, BigDecimal.valueOf(100));
        verify(cashClient, times(1)).deposit(any(CashOperationDto.class));
    }

    @Test
    void withdraw_ShouldCallCashClient() {
        financeService.withdraw("john", Currency.RUB, BigDecimal.valueOf(50));
        verify(cashClient, times(1)).withdraw(any(CashOperationDto.class));
    }

    @Test
    void transfer_ShouldCallTransferClient() {
        financeService.transfer("john", "mary", Currency.USD, Currency.RUB, BigDecimal.valueOf(10));
        verify(transferClient, times(1)).transfer(any(TransferRequestDto.class));
    }

    @Test
    void transfer_ShouldThrowException_WhenAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                financeService.transfer("john", "mary", Currency.USD, Currency.RUB, BigDecimal.valueOf(-5))
        );
    }

    @Test
    void deposit_ShouldThrowRuntimeException_WhenFeignErrorOccurs() {
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"))
                .when(cashClient).deposit(any(CashOperationDto.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                financeService.deposit("john", Currency.USD, BigDecimal.TEN)
        );
        assertTrue(ex.getMessage().contains("Ошибка"));
    }
}
