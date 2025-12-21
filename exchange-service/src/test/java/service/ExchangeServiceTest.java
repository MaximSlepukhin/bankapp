package service;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.ExchangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeServiceTest {
    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        exchangeService = new ExchangeService();
        exchangeService.consumeRates(new CurrencyRate(Currency.USD, Currency.RUB, BigDecimal.valueOf(95)));
        exchangeService.consumeRates(new CurrencyRate(Currency.CNY, Currency.RUB, BigDecimal.valueOf(15)));
    }

    @Test
    void convert_sameCurrency_shouldReturnSameAmount() {
        ConvertRequest request = new ConvertRequest();
        request.setFrom(Currency.USD);
        request.setTo(Currency.USD);
        request.setAmount(BigDecimal.valueOf(10));

        ConvertResponse response = exchangeService.convert(request);

        assertEquals(BigDecimal.valueOf(10).setScale(6), response.getConverted());
    }

    @Test
    void convert_USDtoRUB_shouldConvertCorrectly() {
        ConvertRequest request = new ConvertRequest();
        request.setFrom(Currency.USD);
        request.setTo(Currency.RUB);
        request.setAmount(BigDecimal.valueOf(10));

        ConvertResponse response = exchangeService.convert(request);

        assertEquals(new BigDecimal("950.000000"), response.getConverted());
    }

    @Test
    void convert_missingRate_shouldThrowException() {
        ConvertRequest request = new ConvertRequest();
        request.setFrom(Currency.USD);
        request.setTo(Currency.CNY);
        request.setAmount(BigDecimal.valueOf(10));

        assertThrows(IllegalArgumentException.class, () -> exchangeService.convert(request));
    }
}
