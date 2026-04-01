package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос на конвертацию валюты")
public class ConvertRequest {

    @Schema(description = "Исходная валюта", example = "USD")
    private Currency from;

    @Schema(description = "Целевая валюта", example = "EUR")
    private Currency to;

    @Schema(description = "Сумма для конвертации", example = "100.00")
    private BigDecimal amount;
}