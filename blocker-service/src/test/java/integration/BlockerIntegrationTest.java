package integration;

import com.github.maximslepukhin.BlockerServiceApplication;
import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import com.github.maximslepukhin.service.BlockerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = BlockerServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=test",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        }
)
@ActiveProfiles("test")
class BlockerIntegrationTest {

    @Autowired
    private BlockerService blockerService;

    @Test
    void contextLoads() {
        assertThat(blockerService).isNotNull();
    }

    @Test
    void checkBlock_usesConfiguredMaintenanceWindow() {
        BlockerStatus status = blockerService.checkBlock(new BlockerRequest("demo"));
        assertThat(status).isNotNull();
        assertThat(status.getReason()).isNotBlank();
    }
}
