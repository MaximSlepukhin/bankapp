package com.github.maximslepukhin.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="rates")
public class ExchangeRate {
    @Id
    @GeneratedValue
    Long id;
    String currency; // e.g. "USD"
    BigDecimal buy;  // bank buys
    BigDecimal sell; // bank sells
    Instant updatedAt;
}