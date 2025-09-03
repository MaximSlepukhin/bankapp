package com.github.maximslepukhin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private String currency;  // <- код валюты, например "USD"
    private String title;     // название валюты
    private BigDecimal value;
    private boolean exists;
}
