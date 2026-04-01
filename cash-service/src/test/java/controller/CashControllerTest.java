package controller;

import com.github.maximslepukhin.controller.CashController;
import com.github.maximslepukhin.idempotency.IdempotencyService;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.service.CashService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import io.micrometer.tracing.Tracer;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CashController.class)
@ContextConfiguration(classes = com.github.maximslepukhin.CashServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CashService cashService;

    @MockBean
    private IdempotencyService idempotencyService;

    @MockBean
    private Tracer tracer;

    @Test
    void deposit_ShouldReturnOk() throws Exception {
        String json = """
                {
                  "currency": "RUB",
                  "amount": 100,
                  "login": "user1"
                }
                """;

        when(idempotencyService.find(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/cash/deposit")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(cashService).deposit(any(CashOperationDto.class), any(UUID.class));
    }

    @Test
    void withdraw_ShouldReturnOk() throws Exception {
        String json = """
                {
                  "currency": "USD",
                  "amount": 50,
                  "login": "john"
                }
                """;

        when(idempotencyService.find(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/cash/withdraw")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(cashService).withdraw(any(CashOperationDto.class), any(UUID.class));
    }

    @Test
    void deposit_ShouldReturnOk_WhenIdempotencyKeyAlreadyCompleted() throws Exception {
        String json = """
                {
                  "currency": "RUB",
                  "amount": 100,
                  "login": "user1"
                }
                """;

        com.github.maximslepukhin.model.entity.IdempotencyKey ik =
                com.github.maximslepukhin.model.entity.IdempotencyKey.builder()
                        .key(UUID.randomUUID())
                        .status(com.github.maximslepukhin.model.enums.IdempotencyStatus.COMPLETED)
                        .httpStatus(200)
                        .build();

        when(idempotencyService.find(any())).thenReturn(Optional.of(ik));

        mockMvc.perform(post("/api/v1/cash/deposit")
                        .header("X-Idempotency-Key", ik.getKey().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        org.mockito.Mockito.verifyNoInteractions(cashService);
    }
}
