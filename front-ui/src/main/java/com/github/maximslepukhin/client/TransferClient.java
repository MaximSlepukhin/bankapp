package com.github.maximslepukhin.client;

import com.github.maximslepukhin.config.feign.FeignOAuth2Config;
import com.github.maximslepukhin.model.dto.TransferRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "transfer-gateway-client",
        url = "http://gateway:8080",
        configuration = FeignOAuth2Config.class
)

public interface TransferClient {

    @PostMapping("/transfer-service/api/transfer")
    void transfer(@RequestBody TransferRequestDto dto);
}
