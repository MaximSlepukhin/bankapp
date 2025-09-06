package com.github.maximslepukhin.model;

import com.github.maximslepukhin.model.currency.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private BigDecimal value = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    public Account(Currency currency, BigDecimal value, User owner) {
        this.currency = currency;
        this.value = value;
        this.owner = owner;
    }
}


