package com.github.maximslepukhin.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;

@Service
public class AccountsClient {

    private final RestTemplate restTemplate;
    private final String accountsServiceUrl;

    public AccountsClient(RestTemplate restTemplate,
                          @Value("${ACCOUNTS_SERVICE_URL}") String accountsServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountsServiceUrl = accountsServiceUrl; // например: http://accounts-service.default.svc.cluster.local:8081
    }

    public List<String> getCurrencies(String login) {
        String url = String.format("%s/api/accounts/%s/currencies", accountsServiceUrl, login);
        String[] response = restTemplate.getForObject(url, String[].class);
        return response != null ? Arrays.asList(response) : List.of();
    }

    public void debit(String login, String currency, BigDecimal amount) {
        String url = String.format("%s/api/accounts/%s/%s/debit?amount=%s",
                accountsServiceUrl, login, currency, amount);
        restTemplate.postForObject(url, null, Void.class);
    }

    public void credit(String login, String currency, BigDecimal amount) {
        String url = String.format("%s/api/accounts/%s/%s/deposit?amount=%s",
                accountsServiceUrl, login, currency, amount);
        restTemplate.postForObject(url, null, Void.class);
    }
}
