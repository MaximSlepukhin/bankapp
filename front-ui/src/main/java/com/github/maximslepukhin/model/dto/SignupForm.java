package com.github.maximslepukhin.model.dto;

import lombok.Data;

@Data
public class SignupForm {
    private String login;
    private String password;
    private String confirm_password;
    private String name;
    private String birthdate;
}
