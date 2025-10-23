package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.AccountDto;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.model.dto.SignupForm;
import com.github.maximslepukhin.service.ExchangeService;
import com.github.maximslepukhin.service.FinanceService;
import com.github.maximslepukhin.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;
    private final FinanceService financeService;
    private final ExchangeService exchangeService;

    // ------------------ Главная страница ------------------
    @GetMapping("/")
    public String root() {
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String mainPage(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
        UserDto user = userService.getUserFromOidc(oidcUser);
        model.addAttribute("login", user.getLogin());
        model.addAttribute("name", user.getName());
        model.addAttribute("birthdate", user.getBirthdate());
        model.addAttribute("accounts", user.getAccounts());
        model.addAttribute("currency", List.of(Currency.USD, Currency.RUB, Currency.CNY));
        List<UserDto> otherUsers = userService.getOtherUsers(user.getLogin());
        model.addAttribute("users", otherUsers);
        model.addAttribute("passwordErrors", null);
        model.addAttribute("userAccountsErrors", null);
        model.addAttribute("cashErrors", null);
        model.addAttribute("transferErrors", null);
        model.addAttribute("transferOtherErrors", null);
        return "main";
    }

    // ------------------ Регистрация ------------------
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("form", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupForm form, Model model) {
        try {
            userService.registerUser(form);
            return "redirect:/oauth2/authorization/keycloak";
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
            return "signup";
        }
    }

    // ------------------ Работа с балансом ------------------
    @PostMapping("/user/{login}/cash")
    public String cashOperation(
            @PathVariable String login,
            @RequestParam Currency currency,
            @RequestParam BigDecimal value,
            @RequestParam String action,
            RedirectAttributes redirectAttrs) {
        try {
            if ("PUT".equalsIgnoreCase(action)) {
                financeService.deposit(login, currency, value);
            } else if ("GET".equalsIgnoreCase(action)) {
                financeService.withdraw(login, currency, value);
            } else {
                redirectAttrs.addFlashAttribute("cashErrors", List.of("Неизвестное действие"));
            }
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("cashErrors", List.of("Ошибка: " + e.getMessage()));
        }
        return "redirect:/main";
    }

    // ------------------ Переводы ------------------
    @PostMapping("/user/{login}/transfer")
    public String transferMoney(
            @PathVariable String login,
            @RequestParam Currency from_currency,
            @RequestParam Currency to_currency,
            @RequestParam BigDecimal value,
            @RequestParam(required = false) String to_login,
            RedirectAttributes redirectAttrs) {
        try {
            financeService.transfer(
                    login,
                    (to_login == null || to_login.isBlank()) ? login : to_login,
                    from_currency,
                    to_currency,
                    value
            );
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("transferErrors", List.of("Ошибка перевода: " + e.getMessage()));
        }
        return "redirect:/main";
    }

    // ------------------ Курсы валют ------------------
    @GetMapping("/api/rates")
    @ResponseBody
    public List<CurrencyRate> getRates() {
        return exchangeService.getRates();
    }

    // ------------------ Обновление пароля ------------------
    @PostMapping("/user/{login}/editPassword")
    public String editPassword(
            @PathVariable String login,
            @RequestParam String password,
            @RequestParam String confirm_password,
            RedirectAttributes redirectAttrs) {
        if (!password.equals(confirm_password)) {
            redirectAttrs.addFlashAttribute("passwordErrors", List.of("Пароли не совпадают"));
            return "redirect:/main";
        }

        try {
            userService.updatePassword(login, password);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("passwordErrors", List.of("Ошибка смены пароля"));
        }
        return "redirect:/main";
    }

    // ------------------ Обновление данных пользователя ------------------
    @PostMapping("/user/{login}/editUserAccounts")
    public String editUserAccounts(
            @PathVariable String login,
            @RequestParam String name,
            @RequestParam String birthdate,
            @RequestParam(required = false, name = "account") List<String> accounts,
            RedirectAttributes redirectAttrs) {
        try {
            UserDto user = userService.getUserByLogin(login);
            user.setName(name);
            user.setBirthdate(LocalDate.parse(birthdate));
            user.setAccounts(accounts.stream()
                    .map(curr -> AccountDto.builder().currency(Currency.valueOf(curr)).build())
                    .collect(Collectors.toList()));
            userService.updateUser(login, user);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("userAccountsErrors", List.of("Ошибка обновления данных"));
        }
        return "redirect:/main";
    }
}