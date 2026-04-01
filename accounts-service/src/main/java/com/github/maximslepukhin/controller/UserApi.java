package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "Управление пользователями")
public interface UserApi {

    @Operation(summary = "Создать пользователя", description = "Регистрирует нового пользователя в системе")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан")
    @ApiResponse(responseCode = "400", description = "Некорректные данные пользователя")
    @PostMapping
    ResponseEntity<UserDto> create(@RequestBody UserDto userDto);

    @Operation(summary = "Найти пользователя по Keycloak ID", description = "Возвращает пользователя по его идентификатору из Keycloak")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @GetMapping("/keycloak/{keycloakId}")
    ResponseEntity<UserDto> getByKeycloakId(
            @Parameter(description = "Идентификатор пользователя в Keycloak", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
            @PathVariable String keycloakId
    );

    @Operation(summary = "Найти пользователя по логину", description = "Возвращает пользователя по его логину")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @GetMapping("/login/{login}")
    ResponseEntity<UserDto> getByLogin(
            @Parameter(description = "Логин пользователя", example = "john_doe") @PathVariable String login
    );

    @Operation(summary = "Получить пользователя", description = "Возвращает данные пользователя по логину")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @GetMapping("/{login}")
    ResponseEntity<UserDto> getUser(
            @Parameter(description = "Логин пользователя", example = "john_doe") @PathVariable String login
    );

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех зарегистрированных пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен")
    @GetMapping
    ResponseEntity<List<UserDto>> getAllUsers();

    @Operation(summary = "Обновить пользователя", description = "Обновляет данные профиля пользователя")
    @ApiResponse(responseCode = "200", description = "Данные успешно обновлены")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PutMapping("/{login}")
    ResponseEntity<UserDto> updateUser(
            @Parameter(description = "Логин пользователя", example = "john_doe") @PathVariable String login,
            @RequestBody UserDto userDto
    );
}
