package com.github.maximslepukhin.client;

import com.github.maximslepukhin.config.feign.FeignConfig;
import com.github.maximslepukhin.model.dto.CashOperationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "cash-service",
        url = "${CASH_SERVICE_URL:http://cash-service:8082}",
        configuration = FeignConfig.class
)
public interface CashClient {
    @PostMapping("/api/cash/deposit")
    void deposit(@RequestBody CashOperationDto dto);

    @PostMapping("/api/cash/withdraw")
    void withdraw(@RequestBody CashOperationDto dto);
}
