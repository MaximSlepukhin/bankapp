package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConvertRequest {
    private Currency from;
    private Currency to;
    private BigDecimal amount;
}