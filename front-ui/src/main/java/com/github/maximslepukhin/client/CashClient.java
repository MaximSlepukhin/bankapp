
package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.CashOperationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
@Slf4j
@Component
public class CashClient {

    private final RestTemplate restTemplate;

    @Value("${CASH_SERVICE_URL:http://cash-service:8082}")
    private String cashServiceUrl;

    public CashClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void deposit(CashOperationDto dto) {
        String url = cashServiceUrl + "/api/cash/deposit";
        log.info("Выполняется депозит: URL={}, dto={}", url, dto);

        try {
            restTemplate.postForObject(url, dto, Void.class);
            log.info("Депозит успешно выполнен: dto={}", dto);
        } catch (HttpClientErrorException e) {
            log.error("Ошибка при депозите: статус={}, тело ответа={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при депозите: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void withdraw(CashOperationDto dto) {
        String url = cashServiceUrl + "/api/cash/withdraw";
        log.info("Выполняется снятие: URL={}, dto={}", url, dto);

        try {
            restTemplate.postForObject(url, dto, Void.class);
            log.info("Снятие успешно выполнено: dto={}", dto);
        } catch (HttpClientErrorException e) {
            log.error("Ошибка при снятии: статус={}, тело ответа={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при снятии: {}", e.getMessage(), e);
            throw e;
        }
    }

}
