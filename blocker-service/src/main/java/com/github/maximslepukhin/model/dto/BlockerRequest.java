package com.github.maximslepukhin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на проверку блокировки пользователя")
public class BlockerRequest {

    @NotBlank(message = "Поле login не может быть пустым")
    @Schema(description = "Логин пользователя для проверки", example = "john_doe")
    private String login;
}