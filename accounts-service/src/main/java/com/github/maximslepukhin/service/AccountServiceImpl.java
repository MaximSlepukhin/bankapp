package com.github.maximslepukhin.service;

import com.github.maximslepukhin.annotation.FinancialOperation;
import com.github.maximslepukhin.exception.AccountNotFoundException;
import com.github.maximslepukhin.exception.InsufficientFundsException;
import com.github.maximslepukhin.exception.InvalidAmountException;
import com.github.maximslepukhin.exception.UserNotFoundException;
import com.github.maximslepukhin.model.entity.Account;
import com.github.maximslepukhin.model.entity.User;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.repository.AccountRepository;
import com.github.maximslepukhin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    // 🟢 Получение баланса
    @Override
    public BigDecimal getBalance(String login, String currencyStr) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + login));

        Currency currency = Currency.valueOf(currencyStr.toUpperCase());

        return accountRepository.findByUserAndCurrency(user, currency)
                .map(Account::getValue)
                .orElse(BigDecimal.ZERO);
    }

    // 🟠 Списание средств
    @Override
    @FinancialOperation(operation = "DEBIT")
    public void debit(String login, String currencyStr, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Сумма должна быть положительной");
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new AccountNotFoundException("Пользователь не найден"));

        Currency currency = Currency.valueOf(currencyStr.toUpperCase());
        Account account = accountRepository.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new AccountNotFoundException("Счёт в валюте " + currency + " не найден"));


        BigDecimal oldBalance = account.getValue();
        BigDecimal newBalance = oldBalance.subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на счёте");
        }

        account.setValue(newBalance);
        accountRepository.save(account);
    }

    // 🟢 Зачисление средств
    @Override
    @FinancialOperation(operation = "CREDIT")
    public void credit(String login, String currencyStr, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Сумма должна быть положительной");
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + login));

        Currency currency = Currency.valueOf(currencyStr.toUpperCase());
        Account account = accountRepository.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new AccountNotFoundException("Счёт в валюте " + currency + " не найден"));

        BigDecimal oldBalance = account.getValue();
        BigDecimal newBalance = oldBalance.add(amount);

        account.setValue(newBalance);
        accountRepository.save(account);
    }

    // 🟡 Универсальное обновление баланса
    @Override
    @FinancialOperation(operation = "UPDATE_BALANCE")
    public BigDecimal updateAccountBalance(String login, String currencyCode, BigDecimal amount) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + login));

        Currency currency = Currency.valueOf(currencyCode.toUpperCase());

        Account account = user.getAccounts().stream()
                .filter(a -> a.getCurrency() == currency)
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Счёт в валюте " + currency + " не найден"));

        BigDecimal newBalance = account.getValue().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Сумма должна быть положительной");
        }

        account.setValue(newBalance);
        userRepository.save(user);

        return newBalance;
    }

    // 🧾 Список валют пользователя
    @Override
    public List<String> getCurrencies(String login) {
        log.info("➡️ getCurrencies вызван: login={}", login);

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + login));

        List<String> result = user.getAccounts().stream()
                .map(account -> account.getCurrency().name())
                .collect(Collectors.toList());

        log.info("✅ getCurrencies вернул: login={}, currencies={}", login, result);
        return result;
    }
}
