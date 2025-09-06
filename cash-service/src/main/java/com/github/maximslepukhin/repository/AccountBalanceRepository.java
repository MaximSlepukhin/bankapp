package com.github.maximslepukhin.repository;

import com.github.maximslepukhin.model.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {
    Optional<AccountBalance> findByAccountIdAndCurrency(Long accountId, String currency);
}