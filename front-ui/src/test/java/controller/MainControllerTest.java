package controller;

import com.github.maximslepukhin.controller.MainController;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.service.ExchangeService;
import com.github.maximslepukhin.service.FinanceService;
import com.github.maximslepukhin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private UserDto testUser;

    @BeforeEach
    void setup() {
        testUser = UserDto.builder()
                .login("testuser")
                .name("Test User")
                .birthdate(LocalDate.of(2000, 1, 1))
                .accounts(List.of())
                .build();
    }

    @Test
    void mainPage_redirectsToLogin_ifUserNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/main"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }


    @Test
    void signupForm_returnsSignupView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("form"));
    }
}
