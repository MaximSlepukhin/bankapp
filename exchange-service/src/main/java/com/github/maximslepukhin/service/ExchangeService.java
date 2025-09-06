package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.ConvertRequest;
import com.github.maximslepukhin.model.ConvertResponse;
import com.github.maximslepukhin.model.CurrencyRate;
import com.github.maximslepukhin.model.currency.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeService {

    private final Map<String, BigDecimal> rates = new ConcurrentHashMap<>();

    public void updateRates(List<CurrencyRate> newRates) {
        for (CurrencyRate rate : newRates) {
            rates.put(rate.getFrom() + "-" + rate.getTo(), rate.getRate());
        }
        rates.put("RUB-RUB", BigDecimal.ONE);
    }

    public List<CurrencyRate> getRates() {
        List<CurrencyRate> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
            String[] parts = entry.getKey().split("-");
            result.add(new CurrencyRate(Currency.valueOf(parts[0]), Currency.valueOf(parts[1]), entry.getValue()));
        }
        return result;
    }

    public ConvertResponse convert(ConvertRequest request) {
        BigDecimal rate;
        if (request.getFrom() == request.getTo()) {
            rate = BigDecimal.ONE;
        } else {
            BigDecimal fromToRub = request.getFrom() == Currency.RUB ? BigDecimal.ONE :
                    rates.get(request.getFrom() + "-RUB");
            BigDecimal rubToTo = request.getTo() == Currency.RUB ? BigDecimal.ONE :
                    rates.get("RUB-" + request.getTo());
            rate = fromToRub.multiply(rubToTo);
        }

        BigDecimal convertedAmount = request.getAmount().multiply(rate).setScale(6, RoundingMode.HALF_UP);
        return new ConvertResponse(request.getAmount(), convertedAmount);
    }
}
