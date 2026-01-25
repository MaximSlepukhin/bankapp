package service;

import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.KeycloakAdminClient;
import com.github.maximslepukhin.model.dto.SignupForm;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;


import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private AccountsClient accountsClient;
    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_ShouldCreateUserInKeycloakAndAccounts() {
        SignupForm form = new SignupForm();
        form.setLogin("john");
        form.setPassword("pass");
        form.setBirthdate("2000-01-01");
        form.setName("John Doe");

        when(accountsClient.getUserByLogin("john")).thenThrow(new RuntimeException("not found"));
        when(keycloakAdminClient.createUser("john", "pass")).thenReturn("keycloak-123");

        userService.registerUser(form);

        verify(keycloakAdminClient).createUser("john", "pass");
        verify(accountsClient).createUser(any(UserDto.class));
    }
}