package com.github.maximslepukhin.client;

//import com.github.maximslepukhin.config.feign.FeignOAuth2Config;
import com.github.maximslepukhin.config.feign.FeignConfig;
import com.github.maximslepukhin.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "accounts-service", // имя Kubernetes сервиса
        url = "${ACCOUNTS_SERVICE_URL:http://accounts-service:8081}",
        configuration = FeignConfig.class
// можно вынести в переменную
)
public interface AccountsClient {
    @GetMapping("/api/users/keycloak/{keycloakId}")
    UserDto getUserByKeycloakId(@PathVariable String keycloakId);

    @GetMapping("/api/users/login/{login}")
    UserDto getUserByLogin(@PathVariable String login);

    @GetMapping("/api/users")
    List<UserDto> getAllUsers();

    @PostMapping("/api/users")
    void createUser(@RequestBody UserDto user);

    @PutMapping("/api/users/{login}")
    void updateUser(@PathVariable String login, @RequestBody UserDto user);
}