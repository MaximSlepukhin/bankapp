package service;

import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.ExchangeRateGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


class ExchangeRateGeneratorTest {

    private final ExchangeRateGenerator generator = new ExchangeRateGenerator();

    @Test
    void generateRates_shouldReturnNonEmptyRates() {
        List<CurrencyRate> rates = generator.generateRates();

        assertThat(rates).isNotEmpty();
        assertThat(rates).allMatch(rate -> rate.getFrom() != rate.getTo());

        assertThat(rates).allMatch(rate -> rate.getRate().compareTo(java.math.BigDecimal.ZERO) > 0);

        assertThat(rates).anyMatch(rate -> rate.getFrom() == Currency.USD && rate.getTo() == Currency.RUB);
    }
}

