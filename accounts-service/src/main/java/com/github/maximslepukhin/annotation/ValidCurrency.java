package com.github.maximslepukhin.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Constraint-аннотация для валидации кода валюты.
 *
 * Как работает:
 * - @Constraint(validatedBy = ...) связывает аннотацию с классом-валидатором
 * - При нарушении Bean Validation бросает ConstraintViolationException
 * - Spring превращает её в 400 Bad Request (если добавить обработчик в GlobalExceptionHandler)
 *
 * Пример использования:
 *   public ResponseEntity<?> debit(@PathVariable @ValidCurrency String currency, ...)
 */
@Constraint(validatedBy = ValidCurrencyValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidCurrency {

    // Обязательные атрибуты для любой constraint-аннотации
    String message() default "Неизвестная валюта. Допустимые значения: RUB, USD, CNY";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
