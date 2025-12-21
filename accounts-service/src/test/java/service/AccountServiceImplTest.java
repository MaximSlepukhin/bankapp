package service;

import com.github.maximslepukhin.exception.InsufficientFundsException;
import com.github.maximslepukhin.model.entity.Account;
import com.github.maximslepukhin.model.entity.User;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.repository.AccountRepository;
import com.github.maximslepukhin.repository.UserRepository;
import com.github.maximslepukhin.service.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.maximslepukhin.exception.AccountNotFoundException;

class AccountServiceImplTest {

    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        accountRepository = mock(AccountRepository.class);
        accountService = new AccountServiceImpl(userRepository, accountRepository);
    }

    @Test
    void credit_shouldIncreaseBalance() {
        User user = new User();
        Account acc = new Account(1L, Currency.RUB, BigDecimal.valueOf(100), user);
        when(userRepository.findByLogin("alex")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrency(user, Currency.RUB)).thenReturn(Optional.of(acc));

        accountService.credit("alex", "RUB", BigDecimal.valueOf(50));

        assertThat(acc.getValue()).isEqualByComparingTo("150");
        verify(accountRepository).save(acc);
    }

    @Test
    void debit_shouldDecreaseBalance() {
        User user = new User();
        Account acc = new Account(1L, Currency.USD, BigDecimal.valueOf(100), user);
        when(userRepository.findByLogin("alex")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrency(user, Currency.USD)).thenReturn(Optional.of(acc));

        accountService.debit("alex", "USD", BigDecimal.valueOf(40));

        assertThat(acc.getValue()).isEqualByComparingTo("60");
    }

    @Test
    void debit_shouldThrow_whenInsufficientFunds() {
        User user = new User();
        Account acc = new Account(1L, Currency.USD, BigDecimal.valueOf(20), user);
        when(userRepository.findByLogin("alex")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrency(user, Currency.USD)).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.debit("alex", "USD", BigDecimal.valueOf(50)))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void debit_shouldThrow_whenAccountNotFound() {
        User user = new User();
        when(userRepository.findByLogin("alex")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndCurrency(user, Currency.USD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.debit("alex", "USD", BigDecimal.valueOf(10)))
                .isInstanceOf(AccountNotFoundException.class);
    }
}