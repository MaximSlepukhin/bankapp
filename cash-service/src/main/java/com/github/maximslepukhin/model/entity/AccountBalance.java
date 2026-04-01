package com.github.maximslepukhin.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {
    private Long id;
    private Long accountId;
    private String currency;
    private BigDecimal balance;
}
