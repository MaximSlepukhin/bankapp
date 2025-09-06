package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.CurrencyRate;
import com.github.maximslepukhin.model.currency.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
public class ExchangeRateGenerator {

    private final Random random = new Random();

    public List<CurrencyRate> generateRates() {
        List<CurrencyRate> rates = new ArrayList<>();
        for (Currency from : Currency.values()) {
            for (Currency to : Currency.values()) {
                if (from != to) {
                    rates.add(new CurrencyRate(from, to, generateRate(from, to)));
                }
            }
        }
        return rates;
    }

    private BigDecimal generateRate(Currency from, Currency to) {
        BigDecimal baseRate = switch (from) {
            case RUB -> BigDecimal.ONE;
            case USD -> BigDecimal.valueOf(90 + random.nextDouble() * 10);
            case CNY -> BigDecimal.valueOf(15 + random.nextDouble() * 2);
        };

        BigDecimal targetRate = switch (to) {
            case RUB -> BigDecimal.ONE;
            case USD -> BigDecimal.valueOf(90 + random.nextDouble() * 10);
            case CNY -> BigDecimal.valueOf(15 + random.nextDouble() * 2);
        };

        BigDecimal result;
        if (from == Currency.RUB) {
            result = targetRate;
        } else if (to == Currency.RUB) {
            result = BigDecimal.ONE.divide(baseRate, 6, RoundingMode.HALF_UP);
        } else {
            result = targetRate.divide(baseRate, 6, RoundingMode.HALF_UP);
        }

        return result.setScale(6, RoundingMode.HALF_UP);
    }
}

