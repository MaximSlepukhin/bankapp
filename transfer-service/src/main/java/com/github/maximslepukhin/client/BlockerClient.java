package com.github.maximslepukhin.client;

import com.github.maximslepukhin.model.record.BlockerStatus;
import com.github.maximslepukhin.model.dto.TransferRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "blocker-service", path = "/api/blocker")
public interface BlockerClient {

    @PostMapping("/check")
    BlockerStatus check(@RequestBody TransferRequest request);
}
