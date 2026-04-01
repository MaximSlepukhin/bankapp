package com.github.maximslepukhin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Курс валюты для отображения в UI")
public class CurrencyRateDto {

    @Schema(description = "Название валюты", example = "Евро")
    private String title;

    @Schema(description = "Код валюты", example = "EUR")
    private String name;

    @Schema(description = "Курс относительно базовой валюты (RUB)", example = "92.50")
    private BigDecimal value;
}

