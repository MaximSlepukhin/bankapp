package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maximslepukhin.BlockerServiceApplication;
import com.github.maximslepukhin.config.MaintenanceProperties;
import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import com.github.maximslepukhin.service.BlockerService;
import com.github.maximslepukhin.service.BlockerServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = BlockerServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "management.zipkin.tracing.export.enabled=false",
                "management.tracing.sampling.probability=0"
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlockerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlockerService blockerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
        assertThat(blockerService).isNotNull();
    }

    @Test
    void checkBlock_outsideMaintenanceWindow_returnsNotBlocked() {
        // application-test.yaml sets window 01:00–05:00; current time is almost certainly outside
        BlockerStatus status = blockerService.checkBlock(new BlockerRequest("alice"));
        assertThat(status).isNotNull();
        assertThat(status.getReason()).isNotBlank();
    }

    @Test
    void check_endpoint_returns200_whenNotBlocked() throws Exception {
        mockMvc.perform(post("/api/v1/blocker/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BlockerRequest("alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocked").value(false));
    }

    @Test
    void check_endpoint_returns400_whenLoginIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/blocker/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BlockerRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkBlock_insideMaintenanceWindow_returnsBlocked() {
        MaintenanceProperties props = new MaintenanceProperties();
        props.setStart(LocalTime.of(0, 0));
        props.setEnd(LocalTime.of(23, 59));
        BlockerServiceImpl svc = new BlockerServiceImpl(props);

        BlockerStatus status = svc.checkBlock(new BlockerRequest("bob"));

        assertThat(status.isBlocked()).isTrue();
        assertThat(status.getReason()).contains("техобслуживание");
    }
}
