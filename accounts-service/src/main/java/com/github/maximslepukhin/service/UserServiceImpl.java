package com.github.maximslepukhin.service;

import com.github.maximslepukhin.dto.AccountDto;
import com.github.maximslepukhin.dto.UserDto;
import com.github.maximslepukhin.model.Account;
import com.github.maximslepukhin.model.User;
import com.github.maximslepukhin.model.currency.Currency;
import com.github.maximslepukhin.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto getUserByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Преобразуем в DTO
        UserDto dto = new UserDto();
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthdate(user.getBirthdate());

        dto.setAccounts(
                user.getAccounts().stream()
                        .map(account -> new AccountDto(
                                account.getCurrency().name(),
                                account.getCurrency().name(), // можно заменить на human-readable
                                account.getValue(),
                                account.getValue().compareTo(BigDecimal.ZERO) > 0
                        ))
                        .collect(Collectors.toList())
        );

        return dto;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByLogin(userDto.getLogin())) {
            throw new RuntimeException("User with this login already exists");
        }

        User user = new User();
        user.setLogin(userDto.getLogin());
        user.setPassword(passwordEncoder.encode(userDto.getPassword())); // <-- храним хэш
        user.setName(userDto.getName());
        user.setBirthdate(userDto.getBirthdate());

        List<Account> accounts = List.of(
                new Account(Currency.RUB, BigDecimal.ZERO, user),
                new Account(Currency.USD, BigDecimal.ZERO, user),
                new Account(Currency.CNY, BigDecimal.ZERO, user)
        );

        user.setAccounts(accounts);
        userRepository.save(user);

        return getUserByLogin(user.getLogin());
    }
}