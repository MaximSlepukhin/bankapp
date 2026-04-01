package contracts;

import com.github.maximslepukhin.controller.BlockerController;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import com.github.maximslepukhin.service.BlockerService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Базовый класс для сгенерированных контрактных тестов blocker-service.
 *
 * Два контракта → два сгенерированных @Test-метода:
 *   - checkNotBlocked: POST /api/v1/blocker/check {login: alice}    → 200 {blocked: false}
 *   - checkBlocked:    POST /api/v1/blocker/check {login: blocked_user} → 403 {blocked: true}
 *
 * BlockerService мокируется: тест проверяет только HTTP-слой (контроллер),
 * а не бизнес-логику определения блокировки.
 */
public abstract class BlockerContractBase {

    @BeforeEach
    void setup() {
        BlockerService blockerService = mock(BlockerService.class);

        // alice — обычный пользователь, операция разрешена
        // null-check обязателен: Mockito вызывает argThat для null при поиске подходящего стаба
        when(blockerService.checkBlock(argThat(r -> r != null && "alice".equals(r.getLogin()))))
                .thenReturn(new BlockerStatus(false, ""));

        // blocked_user — подозрительный, операция заблокирована
        when(blockerService.checkBlock(argThat(r -> r != null && "blocked_user".equals(r.getLogin()))))
                .thenReturn(new BlockerStatus(true, "Подозрительная активность"));

        RestAssuredMockMvc.standaloneSetup(new BlockerController(blockerService));
    }
}
