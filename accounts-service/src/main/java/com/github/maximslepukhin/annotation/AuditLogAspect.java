package com.github.maximslepukhin.annotation;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * AOP Aspect для @AuditLog и @FinancialOperation.
 *
 * Как работает:
 * - @Aspect — говорит Spring, что это класс с advice-методами
 * - @Component — регистрирует бин в контексте
 * - @Around — перехватывает вызов до и после выполнения метода
 *
 * Pointcut перехватывает методы с @AuditLog ИЛИ @FinancialOperation.
 * Внутри используется AnnotatedElementUtils.getMergedAnnotation — он умеет
 * "читать" аннотации с мета-аннотаций (в отличие от обычного getAnnotation).
 * Это позволяет получить @AuditLog даже если метод помечен @FinancialOperation.
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Around("@annotation(com.github.maximslepukhin.annotation.AuditLog)" +
            " || @annotation(com.github.maximslepukhin.annotation.FinancialOperation)")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        // getMergedAnnotation проходит по мета-аннотациям — именно поэтому
        // @FinancialOperation(operation = "DEBIT") корректно отдаёт "DEBIT"
        AuditLog auditLog = (method != null)
                ? AnnotatedElementUtils.getMergedAnnotation(method, AuditLog.class)
                : null;

        String operationName = (auditLog != null && !auditLog.operation().isEmpty())
                ? auditLog.operation()
                : (method != null ? method.getName() : pjp.getSignature().getName());

        MDC.put("auditOperation", operationName);
        long start = System.currentTimeMillis();

        log.info("[AUDIT] START  operation={} args={}", operationName, pjp.getArgs());

        try {
            Object result = pjp.proceed();
            log.info("[AUDIT] SUCCESS operation={} durationMs={}", operationName, System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("[AUDIT] FAILED  operation={} durationMs={} error={}",
                    operationName, System.currentTimeMillis() - start, e.getMessage());
            throw e;
        } finally {
            MDC.remove("auditOperation");
        }
    }
}
