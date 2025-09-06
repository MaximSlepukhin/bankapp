package com.github.maximslepukhin.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ConvertResponse {
    private BigDecimal originalAmount;
    private BigDecimal convertedAmount;
}
