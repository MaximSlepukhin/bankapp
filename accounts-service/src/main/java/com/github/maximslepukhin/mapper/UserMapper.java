package com.github.maximslepukhin.mapper;

import com.github.maximslepukhin.model.dto.AccountDto;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.model.entity.Account;
import com.github.maximslepukhin.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserDto toDto(User user) {
        return UserDto.builder()
                .keycloakId(user.getKeycloakId())
                .login(user.getLogin())
                .name(user.getName())
                .birthdate(user.getBirthdate())
                .accounts(
                        user.getAccounts() != null
                                ? user.getAccounts().stream()
                                .map(this::toAccountDto)
                                .collect(Collectors.toList())
                                : List.of()
                )
                .build();
    }

    private AccountDto toAccountDto(Account a) {
        return new AccountDto(
                a.getCurrency().name(),
                a.getCurrency().getTitle(),
                a.getValue(),
                true
        );
    }
}

