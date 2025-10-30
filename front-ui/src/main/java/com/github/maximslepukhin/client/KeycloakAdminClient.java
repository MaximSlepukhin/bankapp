package com.github.maximslepukhin.client;

import com.github.maximslepukhin.exception.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.*;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private final Keycloak keycloak;

    @Value("${keycloak.realm:bank-realm}")
    private String realm;

    public String createUser(String username, String password) {
        log.debug("➡ Создание пользователя в Keycloak: {}", username);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);

        try (Response response = keycloak.realm(realm).users().create(user)) {

            if (response.getStatus() == 201) {
                String userId = CreatedResponseUtil.getCreatedId(response);

                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setTemporary(false);
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(password);

                keycloak.realm(realm)
                        .users()
                        .get(userId)
                        .resetPassword(credential);

                log.info("✅ Пользователь {} создан в Keycloak с id={}", username, userId);
                return userId;

            } else if (response.getStatus() == 409) {
                log.warn("❌ Пользователь {} уже существует в Keycloak", username);
                throw new UserAlreadyExistsException("Пользователь " + username + " уже существует");
            } else {
                String error = response.readEntity(String.class);
                log.error("⚠ Ошибка Keycloak при создании пользователя {}: {} - {}", username, response.getStatus(), error);
                throw new RuntimeException("Ошибка Keycloak: " + response.getStatus() + " " + error);
            }
        }
    }

    public void updatePassword(String login, String newPassword) {
        log.debug("➡ Изменение пароля пользователя {} в Keycloak", login);

        try {
            List<UserRepresentation> users = keycloak.realm(realm)
                    .users()
                    .search(login, true);

            if (users == null || users.isEmpty()) {
                log.warn("❌ Пользователь {} не найден в Keycloak", login);
                throw new RuntimeException("Пользователь не найден: " + login);
            }

            String userId = users.get(0).getId();

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);

            keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .resetPassword(credential);

            log.info("✅ Пароль пользователя {} успешно изменён в Keycloak", login);

        } catch (Exception e) {
            log.error("❌ Ошибка при изменении пароля пользователя {}: {}", login, e.getMessage(), e);
            throw new RuntimeException("Ошибка при изменении пароля для " + login, e);
        }
    }
}