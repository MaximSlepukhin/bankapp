package com.github.maximslepukhin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркер для AOP-аудита финансовых операций.
 *
 * Как работает:
 * - @Retention(RUNTIME) — аннотация видна во время выполнения (нужно для AOP)
 * - @Target(METHOD) — можно навешивать только на методы
 * - AuditLogAspect перехватывает вызовы и логирует старт / результат / ошибку
 *
 * Можно использовать напрямую или через @FinancialOperation (мета-аннотацию).
 *
 * Пример:
 *   @AuditLog(operation = "GET_BALANCE")
 *   public BigDecimal getBalance(...) { ... }
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * Название операции для лога. Если не задано — берётся имя метода.
     */
    String operation() default "";
}
