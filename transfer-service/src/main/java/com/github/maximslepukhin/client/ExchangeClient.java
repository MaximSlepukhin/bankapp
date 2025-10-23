package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.dto.ConvertRequest;
import com.github.maximslepukhin.model.dto.ConvertResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "exchange-service")
public interface ExchangeClient {

    @PostMapping("/api/exchange/convert")
    ConvertResponse convert(@RequestBody ConvertRequest request);
}
