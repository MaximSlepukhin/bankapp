package service;

import com.github.maximslepukhin.client.ExchangeClient;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.service.ExchangeService;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExchangeServiceTest {

    @Mock
    private ExchangeClient exchangeClient;

    @InjectMocks
    private ExchangeService exchangeService;

    @Test
    void getRates_ShouldReturnRatesFromClient() {
        MockitoAnnotations.openMocks(this);
        List<CurrencyRate> mockRates = List.of(new CurrencyRate("USD", "Доллар", null));
        when(exchangeClient.getRates()).thenReturn(mockRates);

        List<CurrencyRate> result = exchangeService.getRates();

        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).getTitle());
    }
}
