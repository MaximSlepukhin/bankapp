package controller;

import com.github.maximslepukhin.AccountsServiceApplication;
import com.github.maximslepukhin.controller.AccountController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.maximslepukhin.service.AccountService;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doThrow;


@WebMvcTest(AccountController.class)
@ContextConfiguration(classes = AccountsServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void getBalance_ShouldReturnBalance() throws Exception {
        Mockito.when(accountService.getBalance("john", "USD"))
                .thenReturn(BigDecimal.valueOf(123.45));

        mockMvc.perform(get("/api/accounts/john/USD"))
                .andExpect(status().isOk())
                .andExpect(content().string("123.45"));
    }

    @Test
    void debit_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/accounts/john/USD/debit")
                        .param("amount", "50"))
                .andExpect(status().isOk());
    }

    @Test
    void debit_ShouldReturnBadRequest_WhenError() throws Exception {
        doThrow(new RuntimeException("Ошибка списания"))
                .when(accountService).debit("john", "USD", BigDecimal.valueOf(50));

        mockMvc.perform(post("/api/accounts/john/USD/debit")
                        .param("amount", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка списания"));
    }

    @Test
    void credit_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/accounts/john/USD/deposit")
                        .param("amount", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void updateBalance_ShouldReturnNewBalance() throws Exception {
        Mockito.when(accountService.updateAccountBalance("john", "USD", BigDecimal.valueOf(200)))
                .thenReturn(BigDecimal.valueOf(300));

        mockMvc.perform(post("/api/accounts/john/USD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(300));
    }

    @Test
    void getCurrencies_ShouldReturnList() throws Exception {
        Mockito.when(accountService.getCurrencies("john"))
                .thenReturn(List.of("RUB", "USD"));

        mockMvc.perform(get("/api/accounts/john/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("RUB"))
                .andExpect(jsonPath("$[1]").value("USD"));
    }
}