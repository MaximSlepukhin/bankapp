package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.exception.UserNotFoundException;
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
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<UserDto> getByKeycloakId(@PathVariable String keycloakId) {
        UserDto user = userService.findByKeycloakId(keycloakId);
        if (user == null) {
            throw new UserNotFoundException(
                    "User with keycloakId " + keycloakId + " not found"
            );
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/login/{login}")
    public ResponseEntity<UserDto> getByLogin(@PathVariable String login) {
        return ResponseEntity.ok(userService.findByLogin(login));
    }

    @GetMapping("/{login}")
    public ResponseEntity<UserDto> getUser(@PathVariable String login) {
        return ResponseEntity.ok(userService.getUserByLogin(login));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{login}")
    public ResponseEntity<UserDto> updateUser(@PathVariable String login, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(login, userDto);
        return ResponseEntity.ok(updatedUser);
    }
}
