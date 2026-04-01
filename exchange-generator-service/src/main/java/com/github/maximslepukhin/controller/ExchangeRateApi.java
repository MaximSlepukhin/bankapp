package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.CurrencyRateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Tag(name = "Exchange Rates", description = "Актуальные курсы валют для отображения в UI")
public interface ExchangeRateApi {

    @Operation(summary = "Получить курсы валют", description = "Возвращает актуальный список курсов всех поддерживаемых валют")
    @ApiResponse(responseCode = "200", description = "Курсы валют успешно получены")
    @GetMapping("")
    List<CurrencyRateDto> getRatesForUi();
}
