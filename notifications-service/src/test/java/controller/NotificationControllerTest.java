package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.controller.NotificationController;
import com.github.maximslepukhin.model.dto.NotificationRequest;
import com.github.maximslepukhin.model.entity.Notification;
import com.github.maximslepukhin.service.NotificationService;
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
@ContextConfiguration(classes = com.github.maximslepukhin.NotificationsServiceApplication.class)
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("user1"))
                .andExpect(jsonPath("$.message").value("Hello!"));
    }

    @Test
    void getForUser_shouldReturnListOfNotifications() throws Exception {
        List<Notification> notifications = List.of(
                new Notification(1L, "user1", "msg1", OffsetDateTime.now()),
                new Notification(2L, "user1", "msg2", OffsetDateTime.now())
        );
        Mockito.when(service.getForUser(eq("user1"))).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("msg1"))
                .andExpect(jsonPath("$[1].message").value("msg2"));
    }
}
