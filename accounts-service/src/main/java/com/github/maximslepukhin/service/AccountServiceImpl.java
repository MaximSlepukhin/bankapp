package com.github.maximslepukhin.service;

import com.github.maximslepukhin.exception.AccountNotFoundException;
import com.github.maximslepukhin.exception.InsufficientFundsException;
import com.github.maximslepukhin.exception.InvalidAmountException;
import com.github.maximslepukhin.model.entity.Account;
import com.github.maximslepukhin.model.entity.User;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.repository.AccountRepository;
import com.github.maximslepukhin.repository.UserRepository;
import jakarta.transaction.Transactional;
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
                .orElseThrow(() -> new RuntimeException("User not found"));

        Currency currency = Currency.valueOf(currencyStr.toUpperCase());

        return accountRepository.findByUserAndCurrency(user, currency)
                .map(Account::getValue)
                .orElse(BigDecimal.ZERO);
    }

    // 🟠 Списание средств
    @Override
    @Transactional
    public void debit(String login, String currencyStr, BigDecimal amount) {
        log.info("➡️ debit вызван: login={}, currency={}, amount={}", login, currencyStr, amount);

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

        log.info("✅ debit успешно: login={}, currency={}, oldBalance={}, amount={}, newBalance={}",
                login, currency, oldBalance, amount, newBalance);
    }

    // 🟢 Зачисление средств
    @Override
    @Transactional
    public void credit(String login, String currencyStr, BigDecimal amount) {
        log.info("➡️ credit вызван: login={}, currency={}, amount={}", login, currencyStr, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Currency currency = Currency.valueOf(currencyStr.toUpperCase());
        Account account = accountRepository.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new RuntimeException("Account with currency " + currency + " not found"));

        BigDecimal oldBalance = account.getValue();
        BigDecimal newBalance = oldBalance.add(amount);

        account.setValue(newBalance);
        accountRepository.save(account);

        log.info("✅ credit успешно: login={}, currency={}, oldBalance={}, amount={}, newBalance={}",
                login, currency, oldBalance, amount, newBalance);
    }

    // 🟡 Универсальное обновление баланса
    @Override
    @Transactional
    public BigDecimal updateAccountBalance(String login, String currencyCode, BigDecimal amount) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Currency currency = Currency.valueOf(currencyCode.toUpperCase());

        Account account = user.getAccounts().stream()
                .filter(a -> a.getCurrency() == currency)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account with currency " + currency + " not found"));

        BigDecimal newBalance = account.getValue().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Сумма должна быть положительной");
        }

        account.setValue(newBalance);
        userRepository.save(user);

        log.info("✅ updateAccountBalance: login={}, currency={}, newBalance={}", login, currency, newBalance);
        return newBalance;
    }

    // 🧾 Список валют пользователя
    @Override
    public List<String> getCurrencies(String login) {
        log.info("➡️ getCurrencies вызван: login={}", login);

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> result = user.getAccounts().stream()
                .map(account -> account.getCurrency().name())
                .collect(Collectors.toList());

        log.info("✅ getCurrencies вернул: login={}, currencies={}", login, result);
        return result;
    }
}
