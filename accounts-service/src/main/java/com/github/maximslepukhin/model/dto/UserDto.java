package com.github.maximslepukhin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные пользователя")
public class UserDto {

    @Schema(description = "Идентификатор пользователя в Keycloak", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String keycloakId;

    @Schema(description = "Логин пользователя", example = "john_doe")
    private String login;

    @Schema(description = "Имя пользователя", example = "John Doe")
    private String name;

    @Schema(description = "Дата рождения", example = "1990-05-15")
    private LocalDate birthdate;

    @Schema(description = "Список счетов пользователя")
    private List<AccountDto> accounts;
}
