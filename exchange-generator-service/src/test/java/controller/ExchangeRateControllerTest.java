package controller;

import com.github.maximslepukhin.controller.ExchangeRateController;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.dto.CurrencyRateDto;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.ExchangeRateGenerator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExchangeRateControllerTest {

    @Test
    void getRatesForUi_shouldMapRatesToDto() {
        ExchangeRateGenerator generator = mock(ExchangeRateGenerator.class);

        List<CurrencyRate> mockRates = List.of(
                new CurrencyRate(Currency.RUB, Currency.USD, BigDecimal.valueOf(0.011)),
                new CurrencyRate(Currency.USD, Currency.RUB, BigDecimal.valueOf(90))
        );

        when(generator.generateRates()).thenReturn(mockRates);

        ExchangeRateController controller = new ExchangeRateController(generator);

        List<CurrencyRateDto> dtos = controller.getRatesForUi();

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getTitle()).isEqualTo("Доллар США");
        assertThat(dtos.get(1).getValue()).isEqualByComparingTo(BigDecimal.valueOf(90));

        verify(generator, times(1)).generateRates();
    }
}

