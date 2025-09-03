package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.dto.AccountDto;
import com.github.maximslepukhin.dto.Currency;
import com.github.maximslepukhin.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    private final RestTemplate restTemplate;

    public MainController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String mainPage(Model model, Principal principal) {
        String login = (principal != null) ? principal.getName() : "testuser";

        UserDto user = new UserDto();

        try {
            // Теперь запрос через Gateway по имени сервиса
            String url = "http://ACCOUNTS-SERVICE/api/users/" + login;
            user = restTemplate.getForObject(url, UserDto.class);
        } catch (Exception ex) {
            System.out.println("Не удалось получить данные от accounts-service: " + ex.getMessage());
        }

        // Общая логика добавления данных в модель
        model.addAttribute("login", user.getLogin());
        model.addAttribute("name", user.getName());
        model.addAttribute("birthdate", user.getBirthdate());
        model.addAttribute("accounts", user.getAccounts());
        model.addAttribute("currency", List.of(Currency.USD, Currency.RUB, Currency.EUR)); // пример валют
        model.addAttribute("users", List.of()); // можно будет подгрузить через accounts-service
        model.addAttribute("passwordErrors", null);
        model.addAttribute("userAccountsErrors", null);
        model.addAttribute("cashErrors", null);
        model.addAttribute("transferErrors", null);
        model.addAttribute("transferOtherErrors", null);

        return "main";
    }
}
