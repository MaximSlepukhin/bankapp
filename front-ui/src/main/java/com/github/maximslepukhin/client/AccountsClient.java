package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.UserDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

    @CircuitBreaker(name = "accountsService", fallbackMethod = "getUserByKeycloakIdFallback")
    @Retry(name = "accountsService")
    public UserDto getUserByKeycloakId(String keycloakId) {
        try {
            UserDto user = restTemplate.getForObject(accountsServiceUrl + "/api/v1/users/keycloak/" + keycloakId, UserDto.class);

            if (user != null) {
                log.info("Получен ответ от accounts-service: keycloakId={}, login={}", keycloakId, user.getLogin());
            } else {
                log.warn("Пользователь с keycloakId={} не найден в accounts-service", keycloakId);
            }

            return user;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении данных пользователя", e);
        }
    }

    private UserDto getUserByKeycloakIdFallback(String keycloakId, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при запросе keycloakId={}: {}", keycloakId, t.getMessage());
        throw new RuntimeException("accounts-service недоступен", t);
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "getUserByLoginFallback")
    @Retry(name = "accountsService")
    public UserDto getUserByLogin(String login) {
        String url = accountsServiceUrl + "/api/v1/users/login/" + login;
        return restTemplate.getForObject(url, UserDto.class);
    }

    private UserDto getUserByLoginFallback(String login, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при запросе login={}: {}", login, t.getMessage());
        return null;
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "accountsService")
    public List<UserDto> getAllUsers() {
        String url = accountsServiceUrl + "/api/v1/users";
        ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
                }
        );
        return response.getBody();
    }

    private List<UserDto> getAllUsersFallback(Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при получении списка пользователей: {}", t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "createUserFallback")
    @Retry(name = "accountsService")
    public void createUser(UserDto user) {
        String url = accountsServiceUrl + "/api/v1/users";
        restTemplate.postForObject(url, user, Void.class);
    }

    private void createUserFallback(UserDto user, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при создании пользователя: {}", t.getMessage());
        throw new RuntimeException("accounts-service недоступен", t);
    }

    @CircuitBreaker(name = "accountsService", fallbackMethod = "updateUserFallback")
    @Retry(name = "accountsService")
    public void updateUser(String login, UserDto user) {
        String url = accountsServiceUrl + "/api/v1/users/" + login;
        restTemplate.put(url, user);
    }

    private void updateUserFallback(String login, UserDto user, Throwable t) {
        log.error("Circuit breaker: accounts-service недоступен при обновлении пользователя login={}: {}", login, t.getMessage());
        throw new RuntimeException("accounts-service недоступен", t);
    }
}
