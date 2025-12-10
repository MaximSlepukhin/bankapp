package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Slf4j
@Component
public class AccountsClient {

    private final RestTemplate restTemplate;

    @Value("${ACCOUNTS_SERVICE_URL:http://accounts-service:8081}")
    private String accountsServiceUrl;

    public AccountsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserDto getUserByKeycloakId(String keycloakId) {
        log.info("Отправка GET запроса в accounts-service на URL: {}/api/users/keycloak/{}", accountsServiceUrl, keycloakId);

        try {
            UserDto user = restTemplate.getForObject(accountsServiceUrl + "/api/users/keycloak/" + keycloakId, UserDto.class);

            if (user != null) {
                log.info("Получен ответ от accounts-service для keycloakId={} с данными пользователя: {}", keycloakId, user);
            } else {
                log.warn("Пользователь с keycloakId={} не найден в accounts-service", keycloakId);
            }

            return user;
        } catch (Exception e) {
            log.error("Ошибка при выполнении GET запроса в accounts-service для keycloakId={}", keycloakId, e);
            throw new RuntimeException("Ошибка при получении данных пользователя", e);
        }
    }

    public UserDto getUserByLogin(String login) {
        String url = accountsServiceUrl + "/api/users/login/" + login;
        return restTemplate.getForObject(url, UserDto.class);  // Выполняем GET запрос
    }

    public List<UserDto> getAllUsers() {
        String url = accountsServiceUrl + "/api/users";
        ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
                }
        );
        return response.getBody();
    }

    public void createUser(UserDto user) {
        String url = accountsServiceUrl + "/api/users";
        restTemplate.postForObject(url, user, Void.class);
    }

    public void updateUser(String login, UserDto user) {
        String url = accountsServiceUrl + "/api/users/" + login;
        restTemplate.put(url, user);  // Выполняем PUT запрос
    }
}
