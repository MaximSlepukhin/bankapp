package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на кассовую операцию")
public class CashOperationDto {

    @NotNull
    @Schema(description = "Валюта операции", example = "USD")
    private Currency currency;

    @NotNull
    @Positive
    @Schema(description = "Сумма операции", example = "1000.00")
    private BigDecimal amount;

    @NotBlank
    @Schema(description = "Логин пользователя", example = "john_doe")
    private String login;
}
