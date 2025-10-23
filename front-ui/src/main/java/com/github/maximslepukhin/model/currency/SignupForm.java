package com.github.maximslepukhin.model.currency;

import lombok.Data;

@Data
public class SignupForm {
    private String login;
    private String password;
    private String confirm_password;
    private String name;
    private String birthdate; // Можно оставить String и потом парсить в LocalDate
}
