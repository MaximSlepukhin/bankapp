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

    /**
     * Генерация базовых курсов (RUB = 1, USD ~ 90–100, CNY ~ 12–14).
     * Значение = сколько RUB стоит 1 единица валюты.
     */
    private Map<Currency, BigDecimal> generateBaseRates() {
        BigDecimal usdInRub = BigDecimal.valueOf(90 + random.nextDouble() * 10); // 90–100
        BigDecimal cnyInRub = BigDecimal.valueOf(12 + random.nextDouble() * 2);  // 12–14

        return Map.of(
                Currency.RUB, BigDecimal.ONE,
                Currency.USD, usdInRub,
                Currency.CNY, cnyInRub
        );
    }

    /**
     * Сгенерировать список всех кросс-курсов.
     */
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

    /**
     * Получить курс между двумя валютами.
     */
    public BigDecimal generateRate(Currency from, Currency to) {
        if (from == to) {
            throw new IllegalArgumentException("Нельзя конвертировать одинаковые валюты: " + from);
        }
        var baseRates = generateBaseRates();
        return calculateRate(from, to, baseRates);
    }

    /**
     * Конвертация суммы через базовую валюту (RUB).
     */
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from == to) {
            return amount;
        }
        var baseRates = generateBaseRates();

        // 1. Переводим в рубли
        BigDecimal inRub = amount.multiply(baseRates.get(from));

        // 2. Переводим из рублей в целевую валюту
        BigDecimal result = inRub.divide(baseRates.get(to), 6, RoundingMode.HALF_UP);

        // Добавляем шум ±2%
        double noise = 1 + (random.nextDouble() * 0.04 - 0.02);
        return result.multiply(BigDecimal.valueOf(noise))
                .setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * Кросс-курс через RUB.
     */
    private BigDecimal calculateRate(Currency from, Currency to, Map<Currency, BigDecimal> baseRates) {
        BigDecimal oneUnit = convert(BigDecimal.ONE, from, to); // сколько "to" за 1 "from"
        return oneUnit;
    }
}