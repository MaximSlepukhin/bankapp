package controller;

import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.ExchangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = com.github.maximslepukhin.ExchangeServiceApplication.class,
        properties = {
                "spring.kafka.bootstrap-servers=localhost:9092", // фиктивный сервер
                "spring.kafka.consumer.auto-startup=false"
        }
)
@AutoConfigureMockMvc
class ExchangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        addRate(Currency.USD, Currency.RUB, BigDecimal.valueOf(95));
        addRate(Currency.CNY, Currency.RUB, BigDecimal.valueOf(15));
    }

    private void addRate(Currency from, Currency to, BigDecimal rate) {
        exchangeService.consumeRates(new CurrencyRate(from, to, rate));
        if (from != Currency.RUB) {
            BigDecimal reverseRate = BigDecimal.ONE.divide(rate, 10, RoundingMode.HALF_UP);
            exchangeService.consumeRates(new CurrencyRate(to, from, reverseRate));
        }
    }

    @Test
    void convertEndpoint_USDtoRUB_shouldReturnConvertedAmount() throws Exception {
        String body = """
                {
                    "from": "USD",
                    "to": "RUB",
                    "amount": 10
                }
                """;

        mockMvc.perform(post("/api/v1/exchange/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.converted").value(950.0));
    }

    @Test
    void convertEndpoint_RUBtoCNY_shouldReturnConvertedAmount() throws Exception {
        String body = """
                {
                    "from": "RUB",
                    "to": "CNY",
                    "amount": 150
                }
                """;

        mockMvc.perform(post("/api/v1/exchange/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.converted").value(10.0));
    }

    @Test
    void convertEndpoint_USDtoCNY_shouldReturnConvertedAmountThroughRUB() throws Exception {
        String body = """
                {
                    "from": "USD",
                    "to": "CNY",
                    "amount": 10
                }
                """;

        mockMvc.perform(post("/api/v1/exchange/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.converted").value(63.333333));
    }

    @Test
    void convertEndpoint_sameCurrency_shouldReturnSameAmount() throws Exception {
        String body = """
                {
                    "from": "USD",
                    "to": "USD",
                    "amount": 123.45
                }
                """;

        mockMvc.perform(post("/api/v1/exchange/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.converted").value(123.45));
    }
}
