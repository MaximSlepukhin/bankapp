package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.TransferRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
public class TransferClient {

    private final RestTemplate restTemplate;

    @Value("${TRANSFER_SERVICE_URL:http://transfer-service:8083}")
    private String transferServiceUrl;

    public TransferClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void transfer(TransferRequestDto dto) {
        String url = transferServiceUrl + "/api/transfer";

        log.info("Отправка запроса на перевод. URL: {}", url);
        log.info("Данные для перевода: {}", dto);

        try {
            // Выполняем POST запрос
            restTemplate.postForObject(url, dto, Void.class);
            log.info("Запрос успешно выполнен");
        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса на перевод", e);
        }
    }
}
