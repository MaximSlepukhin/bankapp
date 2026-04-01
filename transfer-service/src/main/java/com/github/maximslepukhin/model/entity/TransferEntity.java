package com.github.maximslepukhin.model.entity;

import com.github.maximslepukhin.model.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;


    private String fromAccountId;
    private String toAccountId;

    private BigDecimal debited;
    private BigDecimal credited;

    private String currencyFrom;
    private String currencyTo;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private Instant createdAt;

    @Column(length = 500)
    private String compensationReason;
}
