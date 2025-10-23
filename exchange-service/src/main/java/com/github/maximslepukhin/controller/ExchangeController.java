package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping("/rates")
    public List<CurrencyRate> getRates() {
        return exchangeService.getRates();
    }

    @PostMapping("/rates") // ✅ новый эндпоинт
    public void updateRates(@RequestBody List<CurrencyRate> rates) {
        exchangeService.updateRates(rates);
    }

    @PostMapping("/convert")
    public ConvertResponse convert(@RequestBody ConvertRequest request) {
        log.info("📩 Получен запрос на конвертацию: amount={}, from={}, to={}",
                request.getAmount(), request.getFrom(), request.getTo());
        return exchangeService.convert(request);
    }
}