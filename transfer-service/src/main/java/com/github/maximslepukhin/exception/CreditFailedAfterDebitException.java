package com.github.maximslepukhin.exception;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Бросается когда debit прошёл успешно, но credit упал.
 * Несёт данные, необходимые для компенсации (возврата денег отправителю).
 * @Retry в TransferServiceImpl перехватит это исключение и повторит попытку.
 * Если все попытки исчерпаны — fallbackTransfer запустит компенсацию.
 */
@Getter
public class CreditFailedAfterDebitException extends RuntimeException {

    private final String fromLogin;
    private final String fromCurrency;
    private final BigDecimal debited;
    private final UUID compensateKey;

    public CreditFailedAfterDebitException(String message, String fromLogin,
                                            String fromCurrency, BigDecimal debited,
                                            UUID compensateKey) {
        super(message);
        this.fromLogin = fromLogin;
        this.fromCurrency = fromCurrency;
        this.debited = debited;
        this.compensateKey = compensateKey;
    }
}
