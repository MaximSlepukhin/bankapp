package com.github.maximslepukhin.scheduler;

import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.service.ExchangeRateGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateGenerator generator;
    private final KafkaTemplate<String, CurrencyRate> kafkaTemplate;

    @Value("${kafka.topic.exchange-rates:exchange-rates}")
    private String topic;

    @Scheduled(fixedRate = 1000)
    public void generateAndSendRates() {
        List<CurrencyRate> rates = generator.generateRates();
        for (CurrencyRate rate : rates) {
            log.info("📤 Отправка курса в Kafka: key={}, value={}", rate.getFrom().name(), rate);
            kafkaTemplate.send(topic, rate.getFrom().name(), rate);
        }
    }
}
