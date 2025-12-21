package integration;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import com.github.maximslepukhin.model.enums.Currency;
import com.github.maximslepukhin.service.ExchangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
        classes = com.github.maximslepukhin.ExchangeServiceApplication.class,
        properties = {
                "spring.kafka.bootstrap-servers=localhost:9092",
                "spring.kafka.consumer.auto-startup=false"
        }
)
@ActiveProfiles("test")
class ExchangeServiceIntegrationTest {

    @Autowired
    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        exchangeService.consumeRates(new CurrencyRate(Currency.USD, Currency.RUB, BigDecimal.valueOf(95)));
        exchangeService.consumeRates(new CurrencyRate(Currency.CNY, Currency.RUB, BigDecimal.valueOf(15)));

        BigDecimal rubToCny = BigDecimal.ONE.divide(BigDecimal.valueOf(15), 10, BigDecimal.ROUND_HALF_UP);
        exchangeService.consumeRates(new CurrencyRate(Currency.RUB, Currency.CNY, rubToCny));
    }

    @Test
    void convert_USDtoRUB_shouldReturnCorrectValue() {
        ConvertRequest request = new ConvertRequest();
        request.setFrom(Currency.USD);
        request.setTo(Currency.RUB);
        request.setAmount(BigDecimal.valueOf(10));

        ConvertResponse response = exchangeService.convert(request);

        assertThat(response.getConverted()).isEqualByComparingTo("950.000000");
    }

    @Test
    void convert_RUBtoCNY_shouldReturnCorrectValue() {
        ConvertRequest request = new ConvertRequest();
        request.setFrom(Currency.RUB);
        request.setTo(Currency.CNY);
        request.setAmount(BigDecimal.valueOf(150));

        ConvertResponse response = exchangeService.convert(request);

        BigDecimal expected = new BigDecimal("10.000000");
        assertThat(response.getConverted().subtract(expected).abs())
                .isLessThan(new BigDecimal("0.0001"));
    }
}
