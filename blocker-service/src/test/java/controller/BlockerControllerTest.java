package controller;

import com.github.maximslepukhin.controller.BlockerController;
import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import com.github.maximslepukhin.service.BlockerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlockerController.class)
@ContextConfiguration(classes = com.github.maximslepukhin.BlockerServiceApplication.class)
class BlockerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlockerService blockerService;

    @Test
    void shouldReturnForbiddenWhenBlocked() throws Exception {
        Mockito.when(blockerService.checkBlock(any(BlockerRequest.class)))
                .thenReturn(new BlockerStatus(true, "Maintenance"));

        mockMvc.perform(post("/api/blocker/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"user1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.blocked").value(true))
                .andExpect(jsonPath("$.reason").value("Maintenance"));
    }

    @Test
    void shouldReturnOkWhenAllowed() throws Exception {
        Mockito.when(blockerService.checkBlock(any(BlockerRequest.class)))
                .thenReturn(new BlockerStatus(false, "Allowed"));

        mockMvc.perform(post("/api/blocker/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"user2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.reason").value("Allowed"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginIsBlank() throws Exception {
        mockMvc.perform(post("/api/blocker/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
