package com.github.maximslepukhin.controller;


import com.github.maximslepukhin.model.dto.TransferRequest;
import com.github.maximslepukhin.model.dto.TransferResponse;
import com.github.maximslepukhin.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Tag(name = "Transfers", description = "API для денежных переводов")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(summary = "Совершить перевод")
    public ResponseEntity<TransferResponse> transfer(@RequestBody @Valid TransferRequest request) {
        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }
}