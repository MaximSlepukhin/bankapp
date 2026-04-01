package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Exchange", description = "Конвертация валют по актуальному курсу")
public interface ExchangeApi {

    @Operation(summary = "Конвертировать валюту", description = "Выполняет конвертацию суммы из одной валюты в другую по текущему курсу")
    @ApiResponse(responseCode = "200", description = "Конвертация выполнена успешно")
    @ApiResponse(responseCode = "400", description = "Некорректные данные запроса")
    @PostMapping("/convert")
    ConvertResponse convert(@RequestBody ConvertRequest request);
}
