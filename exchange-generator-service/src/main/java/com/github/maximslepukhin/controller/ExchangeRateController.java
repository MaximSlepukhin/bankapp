package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.CurrencyRateDto;
import com.github.maximslepukhin.service.ExchangeRateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/rates")
public class ExchangeRateController implements ExchangeRateApi {

    private final ExchangeRateGenerator rateGenerator;

    public ExchangeRateController(ExchangeRateGenerator rateGenerator) {
        this.rateGenerator = rateGenerator;
    }

    @Override
    public List<CurrencyRateDto> getRatesForUi() {
        List<CurrencyRateDto> dtoList = rateGenerator.generateRates().stream()
                .map(rate -> new CurrencyRateDto(
                        rate.getTo().getTitle(),
                        rate.getTo().name(),
                        rate.getRate()
                ))
                .collect(Collectors.toList());

        log.info("Отправляем {} валютных курсов для UI: {}", dtoList.size(), dtoList);

        return dtoList;
    }
}
