package com.github.maximslepukhin.Controlelr;

import com.github.maximslepukhin.dto.CashOperationRequest;
import com.github.maximslepukhin.model.AccountBalance;
import com.github.maximslepukhin.service.CashService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountBalance> deposit(@RequestBody CashOperationRequest request) {
        AccountBalance balance = cashService.deposit(request);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountBalance> withdraw(@RequestBody CashOperationRequest request) {
        AccountBalance balance = cashService.withdraw(request);
        return ResponseEntity.ok(balance);
    }
}