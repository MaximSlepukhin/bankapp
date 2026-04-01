package com.github.maximslepukhin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные счёта пользователя")
public class AccountDto {

    @Schema(description = "Код валюты", example = "USD")
    private String currency;

    @Schema(description = "Название валюты", example = "Доллар США")
    private String title;

    @Schema(description = "Текущий баланс", example = "1500.00")
    private BigDecimal value;

    @Schema(description = "Признак того, что счёт открыт", example = "true")
    private boolean exists;
}
