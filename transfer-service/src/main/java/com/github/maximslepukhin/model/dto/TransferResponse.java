package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.TransferStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransferResponse {
    private String transactionId;
    private TransferStatus status;
    private BigDecimal debited;
    private BigDecimal credited;
    private String currencyFrom;
    private String currencyTo;
    private String message;
}
