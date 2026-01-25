package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.CurrencyRateDto;
import com.github.maximslepukhin.service.ExchangeRateGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/rates")
public class ExchangeRateController {

    private final ExchangeRateGenerator rateGenerator;

    public ExchangeRateController(ExchangeRateGenerator rateGenerator) {
        this.rateGenerator = rateGenerator;
    }

    @GetMapping("")
    public List<CurrencyRateDto> getRatesForUi() {
        List<CurrencyRateDto> dtoList = rateGenerator.generateRates().stream()
                .map(rate -> new CurrencyRateDto(
                        rate.getTo().getTitle(),
                        rate.getTo().name(),
                        rate.getRate()
                ))
                .collect(Collectors.toList());
        return dtoList;
    }
}