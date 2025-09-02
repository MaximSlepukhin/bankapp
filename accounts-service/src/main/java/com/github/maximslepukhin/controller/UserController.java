package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.User;
import com.github.maximslepukhin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Получить всех пользователей
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Получить пользователя по логину
    @GetMapping("/{login}")
    public ResponseEntity<User> getUserByLogin(@PathVariable String login) {
        Optional<User> userOpt = userService.findByLogin(login);
        return userOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Регистрация нового пользователя
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam String birthdate // формат YYYY-MM-DD
    ) {
        try {
            User user = userService.registerUser(login, password, name, LocalDate.parse(birthdate));
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}