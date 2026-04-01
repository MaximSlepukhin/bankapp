package integration;

import com.github.maximslepukhin.AccountsServiceApplication;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.model.entity.Account;
import com.github.maximslepukhin.model.entity.User;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.repository.AccountRepository;
import com.github.maximslepukhin.repository.UserRepository;
import com.github.maximslepukhin.service.AccountService;
import com.github.maximslepukhin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = AccountsServiceApplication.class,
        properties = {
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "management.zipkin.tracing.export.enabled=false",
                "management.tracing.sampling.probability=0"
        }
)
@ActiveProfiles("test")
@Testcontainers
class AccountsServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withInitScript("init-schema.sql");

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void clean() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertThat(userService).isNotNull();
        assertThat(accountService).isNotNull();
    }

    @Test
    void createUser_shouldPersistUserWithThreeAccounts() {
        UserDto dto = UserDto.builder()
                .login("alice")
                .keycloakId("kc-alice")
                .name("Alice")
                .birthdate(LocalDate.of(1990, 5, 15))
                .accounts(List.of())
                .build();

        userService.createUser(dto);

        User saved = userRepository.findByLogin("alice").orElseThrow();
        assertThat(saved.getLogin()).isEqualTo("alice");
        assertThat(accountRepository.findAll().stream()
                .filter(a -> a.getUser().getLogin().equals("alice"))
                .count()).isEqualTo(3);
    }

    @Test
    void credit_shouldIncreaseBalance() {
        User user = createTestUser("bob");

        accountService.credit("bob", "RUB", BigDecimal.valueOf(500));

        Account account = accountRepository.findByUserAndCurrency(user, Currency.RUB).orElseThrow();
        assertThat(account.getValue()).isEqualByComparingTo("500");
    }

    @Test
    void debit_shouldDecreaseBalance() {
        User user = createTestUser("charlie");
        accountService.credit("charlie", "USD", BigDecimal.valueOf(200));

        accountService.debit("charlie", "USD", BigDecimal.valueOf(50));

        Account account = accountRepository.findByUserAndCurrency(user, Currency.USD).orElseThrow();
        assertThat(account.getValue()).isEqualByComparingTo("150");
    }

    @Test
    void debit_shouldThrow_whenInsufficientFunds() {
        createTestUser("dave");
        accountService.credit("dave", "USD", BigDecimal.valueOf(10));

        assertThatThrownBy(() -> accountService.debit("dave", "USD", BigDecimal.valueOf(100)))
                .hasMessageContaining("Недостаточно средств");
    }

    @Test
    void getCurrencies_shouldReturnAllCurrencies() {
        createTestUser("eve");
        User user = userRepository.findByLogin("eve").orElseThrow();

        List<String> currencies = accountRepository.findAll().stream()
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .map(a -> a.getCurrency().name())
                .toList();

        assertThat(currencies).containsExactlyInAnyOrder("RUB", "USD", "CNY");
    }

    private User createTestUser(String login) {
        UserDto dto = UserDto.builder()
                .login(login)
                .keycloakId("kc-" + login)
                .name(login)
                .birthdate(LocalDate.of(1990, 1, 1))
                .accounts(List.of())
                .build();
        userService.createUser(dto);
        return userRepository.findByLogin(login).orElseThrow();
    }
}
