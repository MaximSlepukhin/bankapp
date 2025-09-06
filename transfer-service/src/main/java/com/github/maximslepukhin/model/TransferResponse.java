package com.github.maximslepukhin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal originalAmount;
    private BigDecimal convertedAmount;
    private String status; // SUCCESS, FAILED
    private String message;
}
