package com.github.maximslepukhin.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String currency; // вместо java.util.Currency

    private BigDecimal value;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    // Геттеры и сеттеры
    public String getCurrencyCode() { return currency; }
    public void setCurrencyCode(String currencyCode) { this.currency = currencyCode; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}

