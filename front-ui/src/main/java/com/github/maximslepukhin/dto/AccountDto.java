package com.github.maximslepukhin.dto;


import com.github.maximslepukhin.model.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Currency currency; // enum
    private String title;
    private BigDecimal value;
    private boolean exists;
}
