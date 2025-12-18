//package controller;
//
//import com.github.maximslepukhin.controller.CashController;
//import com.github.maximslepukhin.model.dto.CashOperationDto;
//import com.github.maximslepukhin.service.CashService;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.mockito.Mockito.*;
//
//@WebMvcTest(CashController.class)
//@ContextConfiguration(classes = com.github.maximslepukhin.CashServiceApplication.class)
//@AutoConfigureMockMvc(addFilters = false)
//class CashControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private CashService cashService;
//
//    @Test
//    void deposit_ShouldReturnOk() throws Exception {
//        String json = """
//                {
//                  "currency": "RUB",
//                  "amount": 100,
//                  "login": "user1"
//                }
//                """;
//
//        mockMvc.perform(post("/api/cash/deposit")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk());
//
//        verify(cashService).deposit(Mockito.any(CashOperationDto.class));
//    }
//
//    @Test
//    void withdraw_ShouldReturnOk() throws Exception {
//        String json = """
//                {
//                  "currency": "USD",
//                  "amount": 50,
//                  "login": "john"
//                }
//                """;
//
//        mockMvc.perform(post("/api/cash/withdraw")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(json))
//                .andExpect(status().isOk());
//
//        verify(cashService).withdraw(Mockito.any(CashOperationDto.class));
//    }
//}