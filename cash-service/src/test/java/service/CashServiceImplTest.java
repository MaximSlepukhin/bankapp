//package service;
//
//import com.github.maximslepukhin.client.AccountsClient;
//import com.github.maximslepukhin.client.BlockerClient;
//import com.github.maximslepukhin.client.NotificationsClient;
//import com.github.maximslepukhin.exception.OperationBlockedException;
//import com.github.maximslepukhin.exception.OperationFailedException;
//import com.github.maximslepukhin.model.dto.CashOperationDto;
//import com.github.maximslepukhin.model.enums.Currency;
//import com.github.maximslepukhin.service.CashServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.http.HttpStatus;
//
//import java.math.BigDecimal;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class CashServiceImplTest {
//
//    @Mock
//    private AccountsClient accountsClient;
//    @Mock
//    private BlockerClient blockerClient;
//    @Mock
//    private NotificationsClient notificationsClient;
//
//    @InjectMocks
//    private CashServiceImpl cashService;
//
//    private CashOperationDto dto;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        dto = new CashOperationDto(Currency.RUB, BigDecimal.valueOf(100), "user1");
//    }
//
//    @Test
//    void deposit_ShouldThrow_WhenBlocked() {
//        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(true);
//
//        assertThrows(OperationBlockedException.class, () -> cashService.deposit(dto));
//
//        verify(accountsClient, never()).updateBalance(any(), any(), any());
//        verify(notificationsClient, never()).notify(any(), any());
//    }
//
//    @Test
//    void deposit_ShouldCallClients_WhenNotBlocked() {
//        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(false);
//
//        cashService.deposit(dto);
//
//        verify(accountsClient).updateBalance("user1", Currency.RUB, BigDecimal.valueOf(100));
//        verify(notificationsClient).notify("user1", "Пополнение на 100 RUB");
//    }
//
//    @Test
//    void deposit_ShouldThrowOperationFailed_WhenAccountsFails() {
//        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(false);
//        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Ошибка"))
//                .when(accountsClient)
//                .updateBalance(any(), any(), any());
//
//
//        assertThrows(OperationFailedException.class, () -> cashService.deposit(dto));
//
//        verify(notificationsClient, never()).notify(any(), any());
//    }
//
//    @Test
//    void withdraw_ShouldThrow_WhenInsufficientFunds() {
//        when(accountsClient.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(50));
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> cashService.withdraw(dto));
//        assertEquals("Недостаточно средств", ex.getMessage());
//    }
//
//    @Test
//    void withdraw_ShouldThrow_WhenBlocked() {
//        when(accountsClient.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(200));
//        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(true);
//
//        RuntimeException ex = assertThrows(RuntimeException.class, () -> cashService.withdraw(dto));
//        assertEquals("Операция заблокирована", ex.getMessage());
//    }
//
//    @Test
//    void withdraw_ShouldCallClients_WhenOk() {
//        when(accountsClient.getBalance(any(), any())).thenReturn(BigDecimal.valueOf(200));
//        when(blockerClient.isBlocked(any(), any(), any())).thenReturn(false);
//
//        cashService.withdraw(dto);
//
//        verify(accountsClient).updateBalance("user1", Currency.RUB, BigDecimal.valueOf(-100));
//        verify(notificationsClient).notify("user1", "Снятие 100 RUB");
//    }
//}
