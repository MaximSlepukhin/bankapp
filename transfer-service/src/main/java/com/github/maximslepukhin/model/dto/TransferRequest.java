package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос на перевод средств")
public class TransferRequest {

    @NotBlank
    @Schema(description = "Логин отправителя", example = "john_doe")
    private String fromLogin;

    @NotBlank
    @Schema(description = "Логин получателя", example = "jane_doe")
    private String toLogin;

    @NotNull
    @Schema(description = "Валюта списания", example = "USD")
    private Currency fromCurrency;

    @NotNull
    @Schema(description = "Валюта зачисления", example = "EUR")
    private Currency toCurrency;

    @NotNull
    @Positive
    @Schema(description = "Сумма перевода", example = "100.00")
    private BigDecimal amount;
}
