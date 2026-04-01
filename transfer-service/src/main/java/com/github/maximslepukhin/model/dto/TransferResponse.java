package com.github.maximslepukhin.model.dto;

import com.github.maximslepukhin.model.enums.TransferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Результат выполнения перевода")
public class TransferResponse {

    @Schema(description = "Уникальный идентификатор транзакции", example = "txn-abc123")
    private String transactionId;

    @Schema(description = "Статус перевода", example = "SUCCESS")
    private TransferStatus status;

    @Schema(description = "Сумма списанная у отправителя", example = "100.00")
    private BigDecimal debited;

    @Schema(description = "Сумма зачисленная получателю", example = "92.50")
    private BigDecimal credited;

    @Schema(description = "Валюта списания", example = "USD")
    private String currencyFrom;

    @Schema(description = "Валюта зачисления", example = "EUR")
    private String currencyTo;

    @Schema(description = "Сообщение о результате операции", example = "Перевод выполнен успешно")
    private String message;

    @Schema(description = "Причина компенсации (если перевод был отменён)", example = "Счёт получателя недоступен")
    private String compensationReason;
}
