package com.github.maximslepukhin.model.dto;

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
public class UserDto {
    private String keycloakId;
    private String login;
    private String name;
    private LocalDate birthdate;
    private List<AccountDto> accounts;
}
