package com.github.maximslepukhin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="transactions")
public class Transaction {
    @Id
    @GeneratedValue
    Long id;
    Long fromAccountId;
    Long toAccountId;
    BigDecimal amount;
    String currency; // original currency
    BigDecimal convertedAmount;
    LocalDateTime createdAt;
    String status; // PENDING, DONE, REJECTED
}
