package controller;

import com.github.maximslepukhin.controller.MainController;
import com.github.maximslepukhin.service.ExchangeService;
import com.github.maximslepukhin.service.FinanceService;
import com.github.maximslepukhin.service.UserService;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = MainController.class)
@ContextConfiguration(classes = com.github.maximslepukhin.FrontUIApplication.class)
@AutoConfigureMockMvc(addFilters = false)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private FinanceService financeService;
    @MockBean
    private ExchangeService exchangeService;

    @Test
    void getRates_ShouldReturnJsonList() throws Exception {
        when(exchangeService.getRates()).thenReturn(List.of(new CurrencyRate("USD", "Dollar", null)));

        mockMvc.perform(get("/api/rates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("USD"));
    }

    @Test
    void root_ShouldRedirectToMain() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }
}
