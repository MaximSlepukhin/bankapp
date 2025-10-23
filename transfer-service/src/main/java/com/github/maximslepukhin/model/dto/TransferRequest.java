package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank
    private String fromLogin;

    @NotBlank
    private String toLogin;

    @NotNull
    private Currency fromCurrency;

    @NotNull
    private Currency toCurrency;

    @NotNull
    private BigDecimal amount;
}
