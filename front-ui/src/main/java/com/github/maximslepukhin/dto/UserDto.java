package com.github.maximslepukhin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String login;
    private String password;            // для регистрации/смены пароля
    private String confirmPassword;     // для регистрации
    private String name;
    private LocalDate birthdate;
    private List<AccountDto> accounts;
}
