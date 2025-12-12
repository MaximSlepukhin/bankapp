package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.controller.NotificationController;
import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.service.NotificationService;
import config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@ContextConfiguration(classes = {com.github.maximslepukhin.NotificationsServiceApplication.class, TestSecurityConfig.class})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private NotificationService service;

    @Test
    void create_shouldReturnCreatedNotification() throws Exception {
        NotificationRequest request = new NotificationRequest("user1", "Hello!");
        Notification created = Notification.builder()
                .id(1L)
                .login("user1")
                .message("Hello!")
                .createdAt(OffsetDateTime.now())
                .build();

        Mockito.when(service.create(any(NotificationRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Expecting HTTP 200 OK
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("user1"))
                .andExpect(jsonPath("$.message").value("Hello!"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getForUser_shouldReturnListOfNotifications() throws Exception {
        List<Notification> notifications = List.of(
                new Notification(1L, "user1", "msg1", OffsetDateTime.now()),
                new Notification(2L, "user1", "msg2", OffsetDateTime.now())
        );

        Mockito.when(service.getForUser(eq("user1"))).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/user1"))
                .andExpect(status().isOk()) // Expecting HTTP 200 OK
                .andExpect(jsonPath("$[0].message").value("msg1"))
                .andExpect(jsonPath("$[1].message").value("msg2"));
    }

    @Test
    void create_shouldReturnInternalServerError_whenExceptionOccurs() throws Exception {
        NotificationRequest request = new NotificationRequest("user1", "Hello!");

        Mockito.when(service.create(any(NotificationRequest.class)))
                .thenThrow(new RuntimeException("Internal server error"));

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()) // Expecting HTTP 500
                .andExpect(content().string("Error while creating notification: Internal server error")); // Verifying the error message
    }

    @Test
    void getForUser_shouldReturnEmptyList_whenExceptionOccurs() throws Exception {
        Mockito.when(service.getForUser(eq("user1")))
                .thenThrow(new RuntimeException("Error fetching notifications"));

        mockMvc.perform(get("/api/notifications/user1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("[]"));
    }
}
