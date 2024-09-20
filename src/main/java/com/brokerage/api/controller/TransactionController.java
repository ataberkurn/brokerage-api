package com.brokerage.api.controller;

import com.brokerage.api.dto.DepositRequest;
import com.brokerage.api.dto.WithdrawRequest;
import com.brokerage.api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public boolean deposit(@RequestBody DepositRequest request) {
        return transactionService.deposit(request);
    }

    @PostMapping("/withdraw")
    public boolean withdraw(@RequestBody WithdrawRequest request) {
        return transactionService.withdraw(request);
    }
}
