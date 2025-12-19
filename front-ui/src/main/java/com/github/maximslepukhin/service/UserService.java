package com.github.maximslepukhin.service;

import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.KeycloakAdminClient;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.exception.UserAlreadyExistsException;
import com.github.maximslepukhin.model.dto.SignupForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountsClient accountsClient;
    private final KeycloakAdminClient keycloakAdminClient;
    private final RestTemplate restTemplateWithAuth;

    public boolean userExists(String login) {
        try {
            return accountsClient.getUserByLogin(login) != null;
        } catch (Exception e) {
            log.warn("Ошибка при проверке существования пользователя {}: {}", login, e.getMessage());
            return false;
        }
    }


    public void registerUser(SignupForm form) {
        if (userExists(form.getLogin())) {
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }

        log.info("Регистрация нового пользователя: {}", form);

        String keycloakId = keycloakAdminClient.createUser(form.getLogin(), form.getPassword());
        log.info("Пользователь создан в Keycloak с ID: {}", keycloakId);

        UserDto dto = UserDto.builder()
                .keycloakId(keycloakId)
                .login(form.getLogin())
                .name(form.getName())
                .birthdate(LocalDate.parse(form.getBirthdate()))
                .accounts(List.of())
                .build();

        log.info("Отправка данных пользователя в accounts-service: {}", dto);

        try {
            HttpEntity<UserDto> requestEntity = new HttpEntity<>(dto);
            ResponseEntity<String> response = restTemplateWithAuth.exchange(
                    "http://accounts-service:8081/api/users", HttpMethod.POST, requestEntity, String.class);

            log.info("Ответ от accounts-service: {}", response.getStatusCode());
            log.info("Ответное тело: {}", response.getBody());
        } catch (Exception e) {
            log.error("Ошибка при отправке данных пользователя в accounts-service: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при регистрации пользователя в accounts-service", e);
        }
    }

    public void updateUser(String login, UserDto user) {
        accountsClient.updateUser(login, user);
    }

    public UserDto getUserByLogin(String login) {
        return accountsClient.getUserByLogin(login);
    }

    public List<UserDto> getOtherUsers(String currentLogin) {
        try {
            return accountsClient.getAllUsers().stream()
                    .filter(u -> !u.getLogin().equals(currentLogin))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка при получении списка других пользователей: {}", e.getMessage());
            return List.of();
        }
    }

    public void updatePassword(String login, String password) {
        keycloakAdminClient.updatePassword(login, password);
    }

    public UserDto getUserFromOidc(OidcUser oidcUser) {
        if (oidcUser == null) {
            return UserDto.builder()
                    .login("")
                    .name("")
                    .accounts(List.of())
                    .build();
        }

        String keycloakId = oidcUser.getSubject();
        try {
            return accountsClient.getUserByKeycloakId(keycloakId);
        } catch (Exception e) {
            log.error("Ошибка при получении пользователя из accounts-service по keycloakId={}: {}", keycloakId, e.getMessage());
            return UserDto.builder()
                    .login("")
                    .name("")
                    .accounts(List.of())
                    .build();
        }
    }
}
