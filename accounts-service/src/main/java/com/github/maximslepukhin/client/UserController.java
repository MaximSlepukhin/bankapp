package com.github.maximslepukhin.client;

import com.github.maximslepukhin.dto.AccountDto;
import com.github.maximslepukhin.dto.UserDto;
import com.github.maximslepukhin.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{login}")
    public ResponseEntity<UserDto> getUser(@PathVariable String login) {
        try {
            UserDto user = userService.getUserByLogin(login);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            // Пользователь не найден
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Общая ошибка
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        try {
            UserDto createdUser = userService.createUser(userDto);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("errors", List.of(e.getMessage())));
        }
    }
}