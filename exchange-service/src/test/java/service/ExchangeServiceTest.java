package service;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.ExchangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeServiceTest {

    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        exchangeService = new ExchangeService();

        exchangeService.updateRates(List.of(
                new CurrencyRate(Currency.RUB, Currency.USD, new BigDecimal("0.010")),
                new CurrencyRate(Currency.USD, Currency.RUB, new BigDecimal("100")),
                new CurrencyRate(Currency.RUB, Currency.CNY, new BigDecimal("0.08")),
                new CurrencyRate(Currency.CNY, Currency.RUB, new BigDecimal("12.5"))
        ));
    }

    @Test
    void testGetRates() {
        var rates = exchangeService.getRates();
        assertFalse(rates.isEmpty());
        assertTrue(rates.stream().anyMatch(r -> r.getFrom() == Currency.RUB && r.getTo() == Currency.USD));
    }

    @Test
    void testConvertSameCurrency() {
        var req = new ConvertRequest();
        req.setFrom(Currency.RUB);
        req.setTo(Currency.RUB);
        req.setAmount(BigDecimal.TEN);

        ConvertResponse resp = exchangeService.convert(req);

        assertEquals(BigDecimal.TEN.setScale(6), resp.getConverted());
        assertEquals(Currency.RUB, resp.getFrom());
        assertEquals(Currency.RUB, resp.getTo());
    }

    @Test
    void testConvertRubToUsd() {
        var req = new ConvertRequest();
        req.setFrom(Currency.RUB);
        req.setTo(Currency.USD);
        req.setAmount(new BigDecimal("1000"));

        ConvertResponse resp = exchangeService.convert(req);

        assertEquals(new BigDecimal("10.000000"), resp.getConverted());
    }

    @Test
    void testConvertUsdToRub() {
        var req = new ConvertRequest();
        req.setFrom(Currency.USD);
        req.setTo(Currency.RUB);
        req.setAmount(new BigDecimal("1"));

        ConvertResponse resp = exchangeService.convert(req);

        assertEquals(new BigDecimal("100.000000"), resp.getConverted());
    }

    @Test
    void testConvertUsdToCny() {
        var req = new ConvertRequest();
        req.setFrom(Currency.USD);
        req.setTo(Currency.CNY);
        req.setAmount(BigDecimal.ONE);

        ConvertResponse resp = exchangeService.convert(req);

        assertEquals(new BigDecimal("8.000000"), resp.getConverted());
    }

    @Test
    void testConvertUnknownRateThrowsException() {
        var req = new ConvertRequest();
        req.setFrom(Currency.USD);
        req.setTo(Currency.CNY);
        req.setAmount(BigDecimal.ONE);

        exchangeService = new ExchangeService();
        exchangeService.updateRates(List.of(
                new CurrencyRate(Currency.RUB, Currency.USD, new BigDecimal("0.01"))
        ));

        assertThrows(IllegalArgumentException.class, () -> exchangeService.convert(req));
    }
}