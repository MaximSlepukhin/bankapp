package contracts;

import com.github.maximslepukhin.controller.AccountController;
import com.github.maximslepukhin.idempotency.IdempotencyService;
import com.github.maximslepukhin.service.AccountService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Базовый класс для всех сгенерированных контрактных тестов accounts-service.
 *
 * Как это работает:
 *   mvn spring-cloud-contract:generateTests
 *   → читает YAML из src/test/resources/contracts/
 *   → генерирует target/generated-test-sources/contracts/ContractsTest.java
 *   → ContractsTest extends AccountsContractBase
 *
 * Каждый сгенерированный метод (@Test) делает HTTP-запрос через RestAssuredMockMvc
 * к AccountController и сравнивает ответ с тем, что описано в YAML.
 *
 * Здесь:
 *   - поднимаем AccountController в standalone-режиме (без Spring-контекста)
 *   - мокируем зависимости и возвращаем ровно те данные, что описаны в контрактах
 */
public abstract class AccountsContractBase {

    @BeforeEach
    void setup() {
        AccountService accountService       = mock(AccountService.class);
        IdempotencyService idempotencyService = mock(IdempotencyService.class);

        // Данные совпадают с тем, что написано в get_balance.yaml
        when(accountService.getBalance("alice", "USD"))
                .thenReturn(new BigDecimal("1000"));

        // Данные совпадают с тем, что написано в get_currencies.yaml
        when(accountService.getCurrencies("alice"))
                .thenReturn(List.of("USD", "RUB", "CNY"));

        // Передаём контроллер в RestAssuredMockMvc — именно он будет принимать
        // HTTP-запросы от сгенерированных тестов
        RestAssuredMockMvc.standaloneSetup(
                new AccountController(accountService, idempotencyService)
        );
    }
}
