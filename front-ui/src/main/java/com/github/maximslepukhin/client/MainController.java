package com.github.maximslepukhin.client;

import com.github.maximslepukhin.dto.UserDto;
import com.github.maximslepukhin.model.currency.Currency;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
        // Получаем логин текущего пользователя
        String login = (principal != null) ? principal.getName() : null;

        UserDto user = null;

        if (login != null) {
            try {
                // Запрос через Gateway к accounts-service по логину пользователя
                String url = "http://ACCOUNTS-SERVICE/api/users/" + login;
                user = restTemplate.getForObject(url, UserDto.class);
            } catch (Exception ex) {
                System.out.println("Не удалось получить данные от accounts-service: " + ex.getMessage());
            }
        }

        // Если пользователь не найден или login = null, создаём пустой объект
        if (user == null) {
            user = new UserDto();
            user.setAccounts(List.of());
        }

        // Добавляем данные в модель
        model.addAttribute("login", user.getLogin());
        model.addAttribute("name", user.getName());
        model.addAttribute("birthdate", user.getBirthdate());
        model.addAttribute("accounts", user.getAccounts());
        model.addAttribute("currency", List.of(Currency.USD, Currency.RUB, Currency.CNY));
        model.addAttribute("users", List.of()); // можно подгружать список всех пользователей
        model.addAttribute("passwordErrors", null);
        model.addAttribute("userAccountsErrors", null);
        model.addAttribute("cashErrors", null);
        model.addAttribute("transferErrors", null);
        model.addAttribute("transferOtherErrors", null);

        return "main";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("login", "");
        model.addAttribute("name", "");
        model.addAttribute("birthdate", null);
        model.addAttribute("errors", null);
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam String confirm_password,
            @RequestParam String name,
            @RequestParam String birthdate,
            Model model
    ) {
        // Создаём DTO для отправки в Accounts-service
        UserDto newUser = new UserDto();
        newUser.setLogin(login);
        newUser.setName(name);
        newUser.setBirthdate(LocalDate.parse(birthdate));
        newUser.setPassword(password); // если в DTO есть поле для пароля
        newUser.setConfirmPassword(confirm_password); // если есть

        try {
            // POST запрос в Accounts-service для регистрации
            String url = "http://ACCOUNTS-SERVICE/api/users";
            restTemplate.postForObject(url, newUser, Void.class);

            // Успешная регистрация — редирект на главную страницу
            return "redirect:/main";

        } catch (Exception ex) {
            // При ошибке возвращаем форму регистрации с ошибками
            model.addAttribute("login", login);
            model.addAttribute("name", name);
            model.addAttribute("birthdate", birthdate);
            model.addAttribute("errors", List.of("Ошибка регистрации: " + ex.getMessage()));
            return "signup";
        }
    }
}
