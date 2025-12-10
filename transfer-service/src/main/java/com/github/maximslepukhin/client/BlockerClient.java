package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.record.BlockerStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BlockerClient {

    private final RestTemplate restTemplate;
    private final String blockerServiceUrl;

    public BlockerClient(RestTemplate restTemplate,
                         @Value("${BLOCKER_SERVICE_URL}") String blockerServiceUrl) {
        this.restTemplate = restTemplate;
        this.blockerServiceUrl = blockerServiceUrl; // например http://blocker-service.default.svc.cluster.local:8087/api/blocker
    }

    public BlockerStatus check(BlockerRequest request) {
        // Отправляем POST запрос на сервер blocker-service
        return restTemplate.postForObject(blockerServiceUrl + "/api/blocker/check", request, BlockerStatus.class);
    }
}
