package com.brokerage.api.service;

import com.brokerage.api.dto.DepositRequest;
import com.brokerage.api.dto.WithdrawRequest;
import com.brokerage.api.entity.Customer;
import com.brokerage.api.entity.Transaction;
import com.brokerage.api.enumeration.TransactionType;
import com.brokerage.api.exception.InsufficientBalanceException;
import com.brokerage.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;

    @Transactional
    public boolean deposit(DepositRequest request) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }

        Customer customer = customerService.getById(request.customerId());
        customer.setBalance(customer.getBalance().add(request.amount()));
        customerService.save(customer);

        Transaction transaction = new Transaction();
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setCustomer(customer);
        transaction.setAmount(request.amount());
        transactionRepository.save(transaction);
        return true;
    }

    @Transactional
    public boolean withdraw(WithdrawRequest request) {
        Customer customer = customerService.getById(request.customerId());

        if (customer.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        customer.setBalance(customer.getBalance().subtract(request.amount()));
        customerService.save(customer);

        Transaction transaction = new Transaction();
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setCustomer(customer);
        transaction.setAmount(request.amount());
        transactionRepository.save(transaction);
        return true;
    }
}
