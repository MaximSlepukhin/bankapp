package com.github.maximslepukhin.model.entity;

import com.github.maximslepukhin.model.enums.Currency;
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

    /**
     * Версия строки для оптимистичной блокировки.
     *
     * JPA автоматически увеличивает это поле при каждом UPDATE.
     * Если два потока одновременно прочитали account с version=1 и оба пытаются сохранить,
     * первый успеет (version станет 2), второй получит ObjectOptimisticLockingFailureException —
     * транзакция откатывается, клиент получает 409 Conflict.
     *
     * SQL-эффект:
     *   UPDATE accounts SET value = ?, version = 2 WHERE id = ? AND version = 1
     *   → если version уже изменилась, UPDATE затрагивает 0 строк → JPA бросает исключение.
     */
    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User user;

    public Account(Currency currency, BigDecimal value, User user) {
        this.currency = currency;
        this.value = value;
        this.user = user;
    }
}
