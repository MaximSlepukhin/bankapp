package com.github.maximslepukhin.service;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    BigDecimal getBalance(String login, String currency);

    void debit(String login, String currency, BigDecimal amount);

    void credit(String login, String currency, BigDecimal amount);

    BigDecimal updateAccountBalance(String login, String currencyCode, BigDecimal amount);

    List<String> getCurrencies(String login);
}
