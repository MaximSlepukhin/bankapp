package com.github.maximslepukhin.model;

import com.github.maximslepukhin.model.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private Currency currency; // валюта перевода
}