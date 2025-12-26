package com.github.maximslepukhin.service;

import com.github.maximslepukhin.config.kafka.KafkaUserRegistrationProducer;
import com.github.maximslepukhin.model.dto.AccountDto;
import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.mapper.UserMapper;
import com.github.maximslepukhin.model.entity.Account;
import com.github.maximslepukhin.model.entity.User;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaUserRegistrationProducer kafkaUserRegistrationProducer;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           KafkaUserRegistrationProducer kafkaUserRegistrationProducer) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.kafkaUserRegistrationProducer = kafkaUserRegistrationProducer;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        try {
            if (userDto.getLogin() == null || userDto.getLogin().isBlank()) {
                throw new IllegalArgumentException("Логин не может быть пустым");
            }
            if (userDto.getKeycloakId() == null || userDto.getKeycloakId().isBlank()) {
                throw new IllegalArgumentException("KeycloakId не может быть пустым");
            }
            if (userRepository.existsByLogin(userDto.getLogin())) {
                throw new RuntimeException("User with this login already exists");
            }

            User user = new User();
            user.setLogin(userDto.getLogin());
            user.setKeycloakId(userDto.getKeycloakId());
            user.setName(userDto.getName());
            user.setBirthdate(userDto.getBirthdate());

            List<Account> accounts = List.of(
                    new Account(Currency.RUB, BigDecimal.ZERO, user),
                    new Account(Currency.USD, BigDecimal.ZERO, user),
                    new Account(Currency.CNY, BigDecimal.ZERO, user)
            );
            user.setAccounts(accounts);
            userRepository.save(user);
            kafkaUserRegistrationProducer.send(new NotificationRequest(user.getLogin(), "Зарегистрирован пользователь " + user.getLogin()));

            return getUserByLogin(user.getLogin());

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public UserDto getUserByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        UserDto dto = new UserDto();
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthdate(user.getBirthdate());

        dto.setAccounts(
                user.getAccounts().stream()
                        .map(account -> new AccountDto(
                                account.getCurrency().name(),
                                account.getCurrency().name(),
                                account.getValue(),
                                account.getValue().compareTo(BigDecimal.ZERO) > 0
                        ))
                        .collect(Collectors.toList())
        );

        return dto;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findByLogin(String login) {
        return userRepository.findByLogin(login)
                .map(userMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found by login: " + login));
    }

    @Override
    public UserDto findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found by keycloakId: " + keycloakId));
    }

    @Override
    public UserDto updateUser(String login, UserDto userDto) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + login));

        log.info("🔄 Обновление пользователя {}", login);

        user.setName(userDto.getName());
        user.setBirthdate(userDto.getBirthdate());

        if (userDto.getAccounts() != null) {
            Set<Currency> requestedCurrencies = userDto.getAccounts().stream()
                    .map(accDto -> Currency.valueOf(accDto.getCurrency()))
                    .collect(Collectors.toSet());

            user.getAccounts().removeIf(acc ->
                    !requestedCurrencies.contains(acc.getCurrency()));

            for (Currency currency : requestedCurrencies) {
                boolean exists = user.getAccounts().stream()
                        .anyMatch(acc -> acc.getCurrency() == currency);
                if (!exists) {
                    Account newAccount = new Account(currency, BigDecimal.ZERO, user);
                    user.getAccounts().add(newAccount);
                    log.debug("➕ Добавлен новый счёт {} пользователю {}", currency, login);
                }
            }
        }

        userRepository.save(user);
        log.info("✅ Пользователь {} успешно обновлён", login);

        return userMapper.toDto(user);
    }
}
