package com.brokerage.api.service;

import com.brokerage.api.dto.DepositRequest;
import com.brokerage.api.dto.WithdrawRequest;
import com.brokerage.api.entity.Asset;
import com.brokerage.api.entity.Customer;
import com.brokerage.api.entity.Transaction;
import com.brokerage.api.enumeration.TransactionType;
import com.brokerage.api.exception.InsufficientBalanceException;
import com.brokerage.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final Logger logger = LogManager.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;
    private final AssetService assetService;

    @Transactional
    public boolean deposit(DepositRequest request) {
        logger.info("Depositing amount: {} for customer: {}", request.amount(), request.customerId());
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        Customer customer = customerService.getById(request.customerId());

        Asset tryAsset = assetService.getAssetByCustomerIdAndName(request.customerId(), "TRY");
        tryAsset.setSize(tryAsset.getSize().add(request.amount()));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(request.amount()));
        assetService.save(tryAsset);

        recordTransaction(customer, request.amount(), TransactionType.DEPOSIT);
        logger.info("Deposit completed successfully for customer: {}", request.customerId());
        return true;
    }

    @Transactional
    public boolean withdraw(WithdrawRequest request) {
        logger.info("Withdrawing amount: {} for customer: {}", request.amount(), request.customerId());
        Customer customer = customerService.getById(request.customerId());

        if (customer.getIban() == null) {
            throw new IllegalStateException("User should have an IBAN");
        }

        Asset tryAsset = assetService.getAssetByCustomerIdAndName(request.customerId(), "TRY");
        if (tryAsset.getUsableSize().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        tryAsset.setSize(tryAsset.getSize().subtract(request.amount()));
        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(request.amount()));
        assetService.save(tryAsset);

        recordTransaction(customer, request.amount(), TransactionType.WITHDRAW);
        logger.info("Withdraw completed successfully for customer: {}", request.customerId());
        return true;
    }

    private void recordTransaction(Customer customer, BigDecimal amount, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setCustomer(customer);
        transaction.setAmount(amount);
        transactionRepository.save(transaction);
    }
}
