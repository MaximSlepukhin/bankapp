package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.dto.AccountDto;
import com.github.maximslepukhin.dto.UserDto;
import com.github.maximslepukhin.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{login}")
    public UserDto getUser(@PathVariable String login) {
//        return userService.getUserByLogin(login);
        System.out.println("Запрос получен в accounts-service для пользователя: " + login);

        UserDto user = new UserDto();
        user.setLogin(login);
        user.setName("Иван Иванов");
        user.setBirthdate(LocalDate.of(1990, 1, 1));

        // Список счетов-заглушек
        List<AccountDto> accounts = List.of(
                new AccountDto("USD", "Доллар", new BigDecimal("150.00"), true),
                new AccountDto("RUB", "Рубль", new BigDecimal("5000.00"), true),
                new AccountDto("EUR", "Евро", BigDecimal.ZERO, false)
        );

        user.setAccounts(accounts);
        return user;
    }
}