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
        log.info("Запрос на внесение денег: dto={}", dto);

        try {
            cashService.deposit(dto);
            log.info("Деньги успешно внесены: dto={}", dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка при внесении денег: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestBody CashOperationDto dto) {
        log.info("Запрос на снятие денег: dto={}", dto);

        try {
            cashService.withdraw(dto);
            log.info("Деньги успешно сняты: dto={}", dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка при снятии денег: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}