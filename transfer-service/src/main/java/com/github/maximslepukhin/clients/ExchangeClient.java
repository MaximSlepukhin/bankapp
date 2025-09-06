package com.github.maximslepukhin.clients;

import com.github.maximslepukhin.model.currency.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExchangeClient {

    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        // REST-запрос к Exchange Service
        BigDecimal rate = from == Currency.USD && to == Currency.RUB ? BigDecimal.valueOf(90) : BigDecimal.ONE;
        return amount.multiply(rate);
    }
}
