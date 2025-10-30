package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.enums.Currency;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;


@Component
public class AccountsClient {

    private final RestTemplate restTemplate;

    public AccountsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BigDecimal getBalance(String login, Currency currency) {
        return restTemplate.getForObject(
                "http://ACCOUNTS-SERVICE/api/accounts/{login}/{currency}",
                BigDecimal.class,
                login,
                currency.name()
        );
    }

    public void updateBalance(String login, Currency currency, BigDecimal amount) {
        restTemplate.postForEntity(
                "http://ACCOUNTS-SERVICE/api/accounts/{login}/{currency}",
                amount,
                Void.class,
                login,
                currency.name()
        );
    }
}
