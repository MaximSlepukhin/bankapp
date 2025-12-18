package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping("/convert")
    public ConvertResponse convert(@RequestBody ConvertRequest request) {
        log.info("📩 Получен запрос на конвертацию: amount={}, from={}, to={}",
                request.getAmount(), request.getFrom(), request.getTo());
        return exchangeService.convert(request);
    }
}