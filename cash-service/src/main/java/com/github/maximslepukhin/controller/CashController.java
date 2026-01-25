package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.CashOperationDto;
import com.github.maximslepukhin.service.CashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@RequestBody CashOperationDto dto) {
        cashService.deposit(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestBody CashOperationDto dto) {
        cashService.withdraw(dto);
        return ResponseEntity.ok().build();
    }
}