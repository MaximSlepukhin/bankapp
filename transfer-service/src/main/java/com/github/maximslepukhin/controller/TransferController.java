package com.github.maximslepukhin.controller;


import com.github.maximslepukhin.model.TransferRequest;
import com.github.maximslepukhin.model.TransferResponse;
import com.github.maximslepukhin.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public TransferResponse transfer(@RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}