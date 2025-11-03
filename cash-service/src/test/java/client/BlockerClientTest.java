package client;

import com.github.maximslepukhin.client.BlockerClient;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.model.record.BlockerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BlockerClientTest {

    private MockRestServiceServer server;
    private BlockerClient blockerClient;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        blockerClient = new BlockerClient(restTemplate);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void shouldReturnTrue_WhenBlocked() throws Exception {
        var response = new BlockerStatus(true, "Limit reached");
        server.expect(requestTo("http://BLOCKER-SERVICE/api/blocker/check"))
                .andRespond(withSuccess(mapper.writeValueAsString(response), MediaType.APPLICATION_JSON));

        boolean blocked = blockerClient.isBlocked("user1", Currency.USD, BigDecimal.TEN);
        assertTrue(blocked);
    }
}
