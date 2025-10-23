package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashOperationDto {
    private Currency currency;
    private BigDecimal amount;
    private String login;
}
