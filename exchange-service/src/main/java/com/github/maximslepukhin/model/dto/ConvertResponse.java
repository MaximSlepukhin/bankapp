package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Результат конвертации валюты")
public class ConvertResponse {

    @Schema(description = "Исходная сумма", example = "100.00")
    private BigDecimal original;

    @Schema(description = "Исходная валюта", example = "USD")
    private Currency from;

    @Schema(description = "Целевая валюта", example = "EUR")
    private Currency to;

    @Schema(description = "Сконвертированная сумма", example = "92.50")
    private BigDecimal converted;
}