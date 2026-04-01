package com.github.maximslepukhin.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Мета-аннотация для финансовых операций.
 *
 * Как работает:
 * - Объединяет @Transactional и @AuditLog в одну аннотацию
 * - @AliasFor позволяет пробрасывать атрибут operation в дочернюю @AuditLog
 * - Spring распознаёт @Transactional через AnnotatedElementUtils.getMergedAnnotation,
 *   который умеет "видеть" аннотации на мета-аннотациях
 *
 * Вместо:
 *   @Transactional
 *   @AuditLog(operation = "DEBIT")
 *   public void debit(...) { ... }
 *
 * Пишем:
 *   @FinancialOperation(operation = "DEBIT")
 *   public void debit(...) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional
@AuditLog
public @interface FinancialOperation {

    /**
     * @AliasFor пробрасывает значение в атрибут operation аннотации @AuditLog.
     * Без него aspect видел бы пустую строку вместо "DEBIT" / "CREDIT" и т.д.
     */
    @AliasFor(annotation = AuditLog.class, attribute = "operation")
    String operation() default "";
}
