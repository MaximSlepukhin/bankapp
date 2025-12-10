package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto userDto) {
        log.info("[accounts-service] Получен запрос на создание пользователя: {}", userDto);
        UserDto savedUser = userService.createUser(userDto);
        log.info("[accounts-service] Пользователь сохранён в БД: {}", savedUser);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<UserDto> getByKeycloakId(@PathVariable String keycloakId) {
        log.info("Получен запрос на поиск пользователя с keycloakId={}", keycloakId);
        try {
            UserDto user = userService.findByKeycloakId(keycloakId);
            if (user != null) {
                log.info("Пользователь с keycloakId={} найден: {}", keycloakId, user);
                return ResponseEntity.ok(user);
            } else {
                log.warn("Пользователь с keycloakId={} не найден", keycloakId);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.error("Ошибка при поиске пользователя с keycloakId={}", keycloakId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/login/{login}")
    public ResponseEntity<UserDto> getByLogin(@PathVariable String login) {
        try {
            return ResponseEntity.ok(userService.findByLogin(login));
        } catch (RuntimeException e) {
            log.warn("Пользователь с login={} не найден", login);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{login}")
    public ResponseEntity<UserDto> getUser(@PathVariable String login) {
        try {
            return ResponseEntity.ok(userService.getUserByLogin(login));
        } catch (RuntimeException e) {
            log.warn("Пользователь с login={} не найден", login);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("➡️ [accounts-service] Запрос на получение всех пользователей");
        try {
            List<UserDto> users = userService.getAllUsers();
            log.info("✅ [accounts-service] Найдено пользователей: {}", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("❌ Ошибка при получении списка пользователей: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{login}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String login, @RequestBody UserDto userDto) {
        log.info("➡️ [accounts-service] Запрос на обновление пользователя {}: {}", login, userDto);
        try {
            UserDto updatedUser = userService.updateUser(login, userDto);
            log.info("✅ [accounts-service] Пользователь {} успешно обновлён", login);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            log.error("❌ Ошибка при обновлении пользователя {}: {}", login, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}
