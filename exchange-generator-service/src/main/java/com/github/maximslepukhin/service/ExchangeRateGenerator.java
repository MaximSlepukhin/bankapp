package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.enums.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class ExchangeRateGenerator {

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    private Map<Currency, BigDecimal> generateBaseRates() {
        BigDecimal usdInRub = BigDecimal.valueOf(90 + random.nextDouble() * 10);
        BigDecimal cnyInRub = BigDecimal.valueOf(12 + random.nextDouble() * 2);

        return Map.of(
                Currency.RUB, BigDecimal.ONE,
                Currency.USD, usdInRub,
                Currency.CNY, cnyInRub
        );
    }

    public List<CurrencyRate> generateRates() {
        var baseRates = generateBaseRates();
        List<CurrencyRate> rates = new ArrayList<>();

        for (Currency from : Currency.values()) {
            for (Currency to : Currency.values()) {
                if (from != to) {
                    BigDecimal rate = calculateRate(from, to, baseRates);
                    rates.add(new CurrencyRate(from, to, rate));
                }
            }
        }
        return rates;
    }

    private BigDecimal calculateRate(Currency from, Currency to, Map<Currency, BigDecimal> baseRates) {
        return baseRates.get(from).divide(baseRates.get(to), 6, RoundingMode.HALF_UP);
    }
}