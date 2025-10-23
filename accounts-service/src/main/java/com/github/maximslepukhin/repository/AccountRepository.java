package com.github.maximslepukhin.repository;

import com.github.maximslepukhin.model.entity.Account;
import com.github.maximslepukhin.model.entity.User;
import com.github.maximslepukhin.model.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserAndCurrency(User user, Currency currency);
}
