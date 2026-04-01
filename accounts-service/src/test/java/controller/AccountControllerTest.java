package controller;

import com.github.maximslepukhin.AccountsServiceApplication;
import com.github.maximslepukhin.controller.AccountController;
import com.github.maximslepukhin.idempotency.IdempotencyService;
import com.github.maximslepukhin.service.AccountService;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@ContextConfiguration(classes = AccountsServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private IdempotencyService idempotencyService;

    @MockBean
    private Tracer tracer;

    @Test
    void getBalance_ShouldReturnBalance() throws Exception {
        Mockito.when(accountService.getBalance("john", "USD"))
                .thenReturn(BigDecimal.valueOf(123.45));

        mockMvc.perform(get("/api/v1/accounts/john/USD"))
                .andExpect(status().isOk())
                .andExpect(content().string("123.45"));
    }

    @Test
    void debit_ShouldReturnOk() throws Exception {
        Mockito.when(idempotencyService.find(Mockito.any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/accounts/john/USD/debit")
                        .param("amount", "50")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void credit_ShouldReturnOk() throws Exception {
        Mockito.when(idempotencyService.find(Mockito.any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/accounts/john/USD/deposit")
                        .param("amount", "100")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrencies_ShouldReturnList() throws Exception {
        Mockito.when(accountService.getCurrencies("john"))
                .thenReturn(List.of("RUB", "USD"));

        mockMvc.perform(get("/api/v1/accounts/john/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("RUB"))
                .andExpect(jsonPath("$[1]").value("USD"));
    }
}
