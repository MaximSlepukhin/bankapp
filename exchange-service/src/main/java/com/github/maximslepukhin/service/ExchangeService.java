package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.enums.Currency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ExchangeService {

    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();

    public void updateRates(List<CurrencyRate> newRates) {
        log.info("POST запрос на /api/exchange/rates получен с телом: {}", rates);
        for (CurrencyRate rate : newRates) {
            rates.put(rate.getFrom() + "-" + rate.getTo(), rate.getRate());
        }
        rates.put("RUB-RUB", BigDecimal.ONE);
    }

    public List<CurrencyRate> getRates() {
        log.info("GET запрос на /api/exchange/rates получен");
        List<CurrencyRate> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
            String[] parts = entry.getKey().split("-");
            result.add(new CurrencyRate(Currency.valueOf(parts[0]), Currency.valueOf(parts[1]), entry.getValue()));
        }
        return result;
    }

    public ConvertResponse convert(ConvertRequest request) {
        log.info("POST запрос на /api/exchange/convert получен с телом: {}", request);
        log.info("➡️ Запрос на конвертацию: amount={}, from={}, to={}",
                request.getAmount(), request.getFrom(), request.getTo());

        BigDecimal rate;

        if (request.getFrom() == request.getTo()) {
            log.info("⚖️ Валюта одинаковая: {} → {}, курс = 1", request.getFrom(), request.getTo());
            rate = BigDecimal.ONE;
        } else {
            BigDecimal fromToRub = request.getFrom() == Currency.RUB
                    ? BigDecimal.ONE
                    : rates.get(request.getFrom() + "-RUB");
            BigDecimal rubToTo = request.getTo() == Currency.RUB
                    ? BigDecimal.ONE
                    : rates.get("RUB-" + request.getTo());

            log.info("🔍 Курс {} → RUB = {}", request.getFrom(), fromToRub);
            log.info("🔍 Курс RUB → {} = {}", request.getTo(), rubToTo);

            if (fromToRub == null || rubToTo == null) {
                log.error("❌ Нет курса для конвертации {} → {}", request.getFrom(), request.getTo());
                throw new IllegalArgumentException("Курс не найден для " + request.getFrom() + " → " + request.getTo());
            }

            rate = fromToRub.multiply(rubToTo);
            log.info("📈 Итоговый курс {} → {} = {}", request.getFrom(), request.getTo(), rate);
        }

        BigDecimal convertedAmount = request.getAmount()
                .multiply(rate)
                .setScale(6, RoundingMode.HALF_UP);

        log.info("✅ Конвертация завершена: {} {} → {} {}",
                request.getAmount(), request.getFrom(), convertedAmount, request.getTo());

        return new ConvertResponse(
                request.getAmount(),
                request.getFrom(),
                request.getTo(),
                convertedAmount
        );
    }
}
