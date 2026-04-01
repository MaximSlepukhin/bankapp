package com.github.maximslepukhin.model.enums;

public enum TransferStatus {
    SUCCESS, FAILED, BLOCKED,
    COMPENSATED,          // деньги возвращены отправителю
    COMPENSATION_FAILED   // компенсация не удалась — нужно ручное вмешательство
}

