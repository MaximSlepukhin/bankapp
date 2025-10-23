package com.github.maximslepukhin.client;

import com.github.maximslepukhin.config.feign.FeignOAuth2Config;
import com.github.maximslepukhin.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "accounts-gateway-client",
        url = "http://gateway:8080",
        configuration = FeignOAuth2Config.class
)

public interface AccountsClient {

    @GetMapping("/accounts-service/api/users/keycloak/{keycloakId}")
    UserDto getUserByKeycloakId(@PathVariable String keycloakId);

    @GetMapping("/accounts-service/api/users/login/{login}")
    UserDto getUserByLogin(@PathVariable String login);

    @GetMapping("/accounts-service/api/users")
    List<UserDto> getAllUsers();

    @PostMapping("/accounts-service/api/users")
    void createUser(@RequestBody UserDto user);

    @PutMapping("/accounts-service/api/users/{login}")
    void updateUser(@PathVariable String login, @RequestBody UserDto user);
}
