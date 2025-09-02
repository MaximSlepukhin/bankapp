package com.github.maximslepukhin.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="bank_accounts")
public class BankAccount {
    @Id
    @GeneratedValue
    Long id;
    @ManyToOne
    User owner;
    @Column(length=3) String currency; // "RUB", "USD", "CNY"
    BigDecimal balance;
}
