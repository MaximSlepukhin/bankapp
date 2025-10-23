package com.github.maximslepukhin.client;

import com.github.maximslepukhin.config.feign.FeignOAuth2Config;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "cash-gateway-client",
        url = "http://gateway:8080",
        configuration = FeignOAuth2Config.class
)

public interface CashClient {

    @PostMapping("/cash-service/api/cash/deposit")
    void deposit(@RequestBody CashOperationDto dto);

    @PostMapping("/cash-service/api/cash/withdraw")
    void withdraw(@RequestBody CashOperationDto dto);
}
