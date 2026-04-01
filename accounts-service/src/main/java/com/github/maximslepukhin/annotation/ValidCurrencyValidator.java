package com.github.maximslepukhin.annotation;

import com.github.maximslepukhin.model.enums.Currency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация логики валидации для @ValidCurrency.
 *
 * ConstraintValidator<A, T>:
 *   A — аннотация, к которой привязан валидатор
 *   T — тип значения, которое проверяется (String в нашем случае)
 */
public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private static final Set<String> VALID_CURRENCIES = Arrays.stream(Currency.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return VALID_CURRENCIES.contains(value.toUpperCase());
    }
}
