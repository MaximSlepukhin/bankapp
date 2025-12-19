package com.github.maximslepukhin.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateDto {
    private String title;
    private String name;
    private BigDecimal value;
}

