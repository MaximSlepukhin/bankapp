package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.ConvertRequest;
import com.github.maximslepukhin.model.ConvertResponse;
import com.github.maximslepukhin.model.CurrencyRate;
import com.github.maximslepukhin.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping("/rates")
    public List<CurrencyRate> getRates() {
        return exchangeService.getRates();
    }

    @PostMapping("/convert")
    public ConvertResponse convert(@RequestBody ConvertRequest request) {
        return exchangeService.convert(request);
    }
}
