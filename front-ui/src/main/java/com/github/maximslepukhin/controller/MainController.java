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
        model.addAttribute("users", userService.getOtherUsers(user.getLogin()));

        // Flash-атрибуты по умолчанию, если не переданы
        model.addAttribute("passwordErrors", model.getAttribute("passwordErrors") != null ? model.getAttribute("passwordErrors") : List.of());
        model.addAttribute("userAccountsErrors", model.getAttribute("userAccountsErrors") != null ? model.getAttribute("userAccountsErrors") : List.of());
        model.addAttribute("cashErrors", model.getAttribute("cashErrors") != null ? model.getAttribute("cashErrors") : List.of());
        model.addAttribute("transferErrors", model.getAttribute("transferErrors") != null ? model.getAttribute("transferErrors") : List.of());
        model.addAttribute("transferOtherErrors", model.getAttribute("transferOtherErrors") != null ? model.getAttribute("transferOtherErrors") : List.of());

        return "main";
    }

    // ------------------ Регистрация ------------------
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("form", new SignupForm());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupForm form, RedirectAttributes redirectAttrs) {
        userService.registerUser(form);
        redirectAttrs.addFlashAttribute("signupSuccess", "Регистрация успешна! Войдите через Keycloak.");
        return "redirect:/oauth2/authorization/keycloak";
    }

    // ------------------ Баланс ------------------
    @PostMapping("/user/{login}/cash")
    public String cashOperation(
            @PathVariable String login,
            @RequestParam Currency currency,
            @RequestParam BigDecimal value,
            @RequestParam String action,
            RedirectAttributes redirectAttrs) {

        try {
            financeService.cashOperation(login, currency, value, action);
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("cashErrors", List.of(e.getMessage()));
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("cashErrors", List.of("Ошибка при операции: " + e.getMessage()));
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
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("transferErrors", List.of(e.getMessage()));
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

    // ------------------ Пароль ------------------
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

        userService.updatePassword(login, password);
        redirectAttrs.addFlashAttribute("passwordSuccess", "Пароль успешно изменён!");
        return "redirect:/main";
    }

    // ------------------ Обновление данных ------------------
    @PostMapping("/user/{login}/editUserAccounts")
    public String editUserAccounts(
            @PathVariable String login,
            @RequestParam String name,
            @RequestParam String birthdate,
            @RequestParam(required = false, name = "account") List<String> accounts,
            RedirectAttributes redirectAttrs) {

        UserDto user = userService.getUserByLogin(login);
        user.setName(name);
        user.setBirthdate(LocalDate.parse(birthdate));
        user.setAccounts(accounts != null
                ? accounts.stream().map(curr -> AccountDto.builder().currency(Currency.valueOf(curr)).build()).collect(Collectors.toList())
                : List.of());
        userService.updateUser(login, user);
        redirectAttrs.addFlashAttribute("userUpdateSuccess", "Профиль обновлён");
        return "redirect:/main";
    }
}