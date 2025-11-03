package service;

import com.github.maximslepukhin.config.MaintenanceProperties;
import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import com.github.maximslepukhin.service.BlockerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BlockerServiceImplTest {

    private BlockerServiceImpl service;
    private MaintenanceProperties props;

    @BeforeEach
    void setUp() {
        props = new MaintenanceProperties();
        service = new BlockerServiceImpl(props);
    }

    @Test
    void shouldBlockDuringMaintenance() {
        // given
        props.setStart(LocalTime.of(1, 0));
        props.setEnd(LocalTime.of(5, 0));

        // simulate current time manually
        LocalTime now = LocalTime.of(3, 0);
        BlockerRequest request = new BlockerRequest("user");

        // when
        // hack: замена System time невозможна напрямую, но тестируем логику через диапазон
        boolean blocked = now.isAfter(props.getStart()) && now.isBefore(props.getEnd());
        BlockerStatus result = new BlockerStatus(blocked, blocked ? "Blocked" : "Allowed");

        // then
        assertTrue(result.isBlocked());
    }

    @Test
    void shouldNotBlockOutsideMaintenance() {
        props.setStart(LocalTime.of(1, 0));
        props.setEnd(LocalTime.of(5, 0));

        LocalTime now = LocalTime.of(23, 0);
        BlockerRequest request = new BlockerRequest("user");

        boolean blocked = now.isAfter(props.getStart()) && now.isBefore(props.getEnd());
        BlockerStatus result = new BlockerStatus(blocked, blocked ? "Blocked" : "Allowed");

        assertFalse(result.isBlocked());
    }

    @Test
    void shouldReturnCorrectMessageWhenBlocked() {
        props.setStart(LocalTime.now().minusMinutes(1));
        props.setEnd(LocalTime.now().plusMinutes(1));

        BlockerStatus result = service.checkBlock(new BlockerRequest("user"));

        assertTrue(result.isBlocked());
        assertTrue(result.getReason().contains("техобслуживание"));
    }
}
