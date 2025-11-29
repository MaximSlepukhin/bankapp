package com.github.maximslepukhin.client;

import com.github.maximslepukhin.config.feign.FeignConfig;
import com.github.maximslepukhin.model.dto.TransferRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "transfer-service",
        url = "${TRANSFER_SERVICE_URL:http://transfer-service:8083}",
        configuration = FeignConfig.class
)
public interface TransferClient {
    @PostMapping("/api/transfer")
    void transfer(@RequestBody TransferRequestDto dto);
}

