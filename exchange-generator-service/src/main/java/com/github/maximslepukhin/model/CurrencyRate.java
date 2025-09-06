package com.github.maximslepukhin.model;

import com.github.maximslepukhin.model.currency.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRate {
    private Currency from;
    private Currency to;
    private BigDecimal rate;

}
