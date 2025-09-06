package com.github.maximslepukhin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CashOperationRequest {

    private Long accountId;
    private String currency;
    private BigDecimal amount;
 }
