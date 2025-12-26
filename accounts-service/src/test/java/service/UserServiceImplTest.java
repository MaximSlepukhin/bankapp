package service;

import com.github.maximslepukhin.config.kafka.KafkaUserRegistrationProducer;
import com.github.maximslepukhin.mapper.UserMapper;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.model.entity.User;
import com.github.maximslepukhin.repository.UserRepository;
import com.github.maximslepukhin.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private UserMapper userMapper;
    private UserServiceImpl userService;
    private  KafkaUserRegistrationProducer kafkaUserRegistrationProducer;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        kafkaUserRegistrationProducer = mock(KafkaUserRegistrationProducer.class);
        userMapper = new UserMapper();
        userService = new UserServiceImpl(userRepository, userMapper, kafkaUserRegistrationProducer);
    }


    @Test
    void createUser_shouldSaveUserWithAccounts() {
        UserDto dto = UserDto.builder()
                .login("alex")
                .keycloakId("kc-123")
                .name("Alex")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();

        when(userRepository.existsByLogin("alex")).thenReturn(false);
        when(userRepository.findByLogin("alex")).thenReturn(Optional.of(new User()));

        userService.createUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getAccounts()).hasSize(3);
        assertThat(saved.getLogin()).isEqualTo("alex");
    }

    @Test
    void createUser_shouldThrow_whenLoginMissing() {
        UserDto dto = new UserDto();
        dto.setKeycloakId("kc-123");
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Логин не может быть пустым");
    }

    @Test
    void updateUser_shouldReplaceAccountsProperly() {
        User existing = User.builder()
                .login("alex")
                .keycloakId("kc-123")
                .build();

        when(userRepository.findByLogin("alex")).thenReturn(Optional.of(existing));

        UserDto dto = UserDto.builder()
                .login("alex")
                .name("Updated Alex")
                .build();

        userService.updateUser("alex", dto);

        verify(userRepository).save(existing);
        assertThat(existing.getName()).isEqualTo("Updated Alex");
    }
}