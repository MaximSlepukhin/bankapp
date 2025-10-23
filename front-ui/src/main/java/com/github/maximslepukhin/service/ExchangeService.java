package com.github.maximslepukhin.service;

import com.github.maximslepukhin.client.ExchangeClient;
import com.github.maximslepukhin.model.dto.CurrencyRate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeClient exchangeClient;

    public List<CurrencyRate> getRates() {
        return exchangeClient.getRates();
    }
}
