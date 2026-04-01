package service;

import com.github.maximslepukhin.client.AccountsClient;
import com.github.maximslepukhin.client.KeycloakAdminClient;
import com.github.maximslepukhin.exception.UserAlreadyExistsException;
import com.github.maximslepukhin.model.dto.SignupForm;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private AccountsClient accountsClient;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @Mock
    private RestTemplate restTemplateWithAuth;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "accountsServiceUrl", "http://accounts-service:8081");
    }

    @Test
    void registerUser_ShouldCreateUserInKeycloakAndSendToAccounts() {
        SignupForm form = new SignupForm();
        form.setLogin("john");
        form.setPassword("pass");
        form.setName("John Doe");
        form.setBirthdate("2000-01-01");

        when(accountsClient.getUserByLogin("john")).thenReturn(null);
        when(keycloakAdminClient.createUser("john", "pass")).thenReturn("keycloak-123");
        ResponseEntity<String> responseEntity = ResponseEntity.ok("OK");
        when(restTemplateWithAuth.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        assertDoesNotThrow(() -> userService.registerUser(form));

        verify(accountsClient).getUserByLogin("john");
        verify(keycloakAdminClient).createUser("john", "pass");
        verify(restTemplateWithAuth).exchange(
                eq("http://accounts-service:8081/api/v1/users"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
    }

    @Test
    void registerUser_ShouldThrowException_IfUserAlreadyExists() {
        SignupForm form = new SignupForm();
        form.setLogin("john");

        when(accountsClient.getUserByLogin("john")).thenReturn(new UserDto());

        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> userService.registerUser(form));

        assertEquals("Пользователь уже существует", ex.getMessage());
        verify(keycloakAdminClient, never()).createUser(anyString(), anyString());
        verify(restTemplateWithAuth, never()).exchange(anyString(), any(), any(), eq(String.class));
    }
}
