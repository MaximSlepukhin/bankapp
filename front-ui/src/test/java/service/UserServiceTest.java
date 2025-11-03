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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void registerUser_ShouldThrow_WhenUserAlreadyExists() {
        SignupForm form = new SignupForm();
        form.setLogin("john");

        when(accountsClient.getUserByLogin("john")).thenReturn(new UserDto());

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(form));
    }

    @Test
    void getOtherUsers_ShouldFilterCurrentUser() {
        var john = UserDto.builder().login("john").build();
        var mary = UserDto.builder().login("mary").build();

        when(accountsClient.getAllUsers()).thenReturn(List.of(john, mary));

        List<UserDto> result = userService.getOtherUsers("john");

        assertEquals(1, result.size());
        assertEquals("mary", result.get(0).getLogin());
    }
}