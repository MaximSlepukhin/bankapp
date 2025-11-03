package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.AccountsServiceApplication;
import com.github.maximslepukhin.controller.UserController;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ContextConfiguration(classes = AccountsServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ отключает Spring Security
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ShouldReturnCreatedUser() throws Exception {
        UserDto input = new UserDto("key1", "john", "John", LocalDate.now(), List.of());
        when(userService.createUser(any(UserDto.class))).thenReturn(input);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("john"));
    }

    @Test
    void getByLogin_ShouldReturnUser() throws Exception {
        UserDto dto = new UserDto("key1", "john", "John", LocalDate.now(), List.of());
        when(userService.findByLogin("john")).thenReturn(dto);

        mockMvc.perform(get("/api/users/login/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(new UserDto("k", "john", "J", LocalDate.now(), List.of())));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].login").value("john"));
    }

    @Test
    void updateUser_ShouldReturnUpdated() throws Exception {
        UserDto updated = new UserDto("k", "john", "Updated", LocalDate.now(), List.of());
        when(userService.updateUser(Mockito.eq("john"), any(UserDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/john")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }
}