package com.github.maximslepukhin.service;

import com.github.maximslepukhin.dto.AccountDto;
import com.github.maximslepukhin.dto.UserDto;
import com.github.maximslepukhin.model.Currency;
import com.github.maximslepukhin.model.User;
import com.github.maximslepukhin.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto getUserByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDto dto = new UserDto();
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthdate(user.getBirthdate());

        dto.setAccounts(
                user.getAccounts().stream()
                        .map(account -> {
                            AccountDto accDto = new AccountDto();
                            accDto.setCurrency(account.getCurrencyCode()); // код валюты

                            // Преобразуем код валюты в enum для получения названия
                            Currency currencyEnum = Currency.valueOf(account.getCurrencyCode());
                            accDto.setTitle(currencyEnum.getTitle());

                            accDto.setValue(account.getValue());
                            accDto.setExists(account.getValue() != null && account.getValue().compareTo(BigDecimal.ZERO) > 0);
                            return accDto;
                        })
                        .collect(Collectors.toList())
        );

        return dto;
    }
}