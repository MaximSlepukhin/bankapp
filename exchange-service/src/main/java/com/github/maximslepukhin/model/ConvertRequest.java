package com.github.maximslepukhin.model;

import com.github.maximslepukhin.model.currency.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConvertRequest {
    private Currency from;
    private Currency to;
    private BigDecimal amount;
}