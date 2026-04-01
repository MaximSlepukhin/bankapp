package com.github.maximslepukhin.service;

import com.github.maximslepukhin.exception.ExchangeRateNotFoundException;
import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.enums.Currency;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;

    @KafkaListener(topics = "exchange-rates", groupId = "exchange-service-group")
    public void consumeRates(CurrencyRate rate) {
        if (rate == null) {
            log.warn("⚠️ Получен null rate из Kafka для {} → {}",
                    rate.getFrom(), rate.getTo());

            meterRegistry.counter("exchange_rate_missing_total",
                    "from", rate.getFrom().name(),
                    "to", rate.getTo().name()
            ).increment();
            return;
        }

        try {
            String key = rate.getFrom() + "-" + rate.getTo();
            rates.put(key, rate.getRate());
            rates.put("RUB-RUB", BigDecimal.ONE);

            log.info("📥 Принят курс из Kafka: {} → {} = {}",
                    rate.getFrom(), rate.getTo(), rate.getRate());

        } catch (Exception e) {
            log.error("Ошибка при обработке курса из Kafka: {}", rate, e);
            meterRegistry.counter("exchange_rate_failed_total",
                    "from", rate.getFrom().name(),
                    "to", rate.getTo().name()
            ).increment();
        }
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
                throw new ExchangeRateNotFoundException("Курс не найден для " + request.getFrom() + " → " + request.getTo());
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