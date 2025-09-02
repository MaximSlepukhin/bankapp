package com.github.maximslepukhin.controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    // --- Главная страница ---
    // Редирект с корня на /main
    @GetMapping("/")
    public String root() {
        return "redirect:/main";
    }

    // Главная страница
    @GetMapping("/main")
    public String main(Model model) {
        model.addAttribute("login", "user123");
        model.addAttribute("name", "Иван Иванов");
        model.addAttribute("birthdate", LocalDate.of(1990, 1, 1));

        // Пока пустые списки для Thymeleaf
        model.addAttribute("accounts", List.of());
        model.addAttribute("currency", List.of());
        model.addAttribute("users", List.of());

        return "main";
    }
}