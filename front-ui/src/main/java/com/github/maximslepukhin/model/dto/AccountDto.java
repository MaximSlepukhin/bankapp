package com.github.maximslepukhin.model.dto;


import com.github.maximslepukhin.model.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Currency currency;
    private String title;
    private BigDecimal value;
    private boolean exists;
}
