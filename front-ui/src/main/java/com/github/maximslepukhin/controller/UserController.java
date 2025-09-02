package com.github.maximslepukhin.controller;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    // Смена пароля
    @PostMapping("/{login}/editPassword")
    public String editPassword(@PathVariable String login,
                               @RequestParam String password,
                               @RequestParam String confirm_password,
                               Model model) {
        // Обработка смены пароля
        // В случае ошибок добавляем их в passwordErrors
        return "redirect:/main";
    }

    // Редактирование аккаунтов
    @PostMapping("/{login}/editUserAccounts")
    public String editUserAccounts(@PathVariable String login,
                                   @RequestParam String name,
                                   @RequestParam String birthdate,
                                   @RequestParam(required = false) String[] account,
                                   Model model) {
        // Обработка изменения аккаунтов
        // В случае ошибок добавляем их в userAccountsErrors
        return "redirect:/main";
    }

    // Внесение/снятие наличных
    @PostMapping("/{login}/cash")
    public String cash(@PathVariable String login,
                       @RequestParam String currency,
                       @RequestParam double value,
                       @RequestParam String action,
                       Model model) {
        // Обработка действия PUT/GET
        // В случае ошибок добавляем их в cashErrors
        return "redirect:/main";
    }

    // Перевод денег (свой и на другого пользователя)
    @PostMapping("/{login}/transfer")
    public String transfer(@PathVariable String login,
                           @RequestParam String from_currency,
                           @RequestParam String to_currency,
                           @RequestParam double value,
                           @RequestParam(required = false) String to_login,
                           Model model) {
        // Обработка перевода
        // Ошибки записываются в transferErrors или transferOtherErrors
        return "redirect:/main";
    }
}