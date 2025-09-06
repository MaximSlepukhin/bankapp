package com.github.maximslepukhin.clients;

import com.github.maximslepukhin.model.currency.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsClient {

    public BigDecimal getBalance(String accountId, Currency currency) {
        // REST-запрос к Accounts Service
        return BigDecimal.valueOf(1000); // пример
    }

    public Currency getAccountCurrency(String accountId) {
        return Currency.RUB; // пример
    }

    public void debit(String accountId, BigDecimal amount, Currency currency) {
        // списание
    }

    public void credit(String accountId, BigDecimal amount, Currency currency) {
        // зачисление
    }
}
