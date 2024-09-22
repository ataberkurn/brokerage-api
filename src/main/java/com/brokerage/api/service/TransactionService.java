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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CustomerService customerService;
    private final AssetService assetService;

    @Transactional
    public boolean deposit(DepositRequest request) {
        if (request.amount() < 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        Customer customer = customerService.getById(request.customerId());

        Asset tryAsset = assetService.getAssetByCustomerIdAndName(request.customerId(), "TRY");
        tryAsset.setSize(tryAsset.getSize() + request.amount());
        tryAsset.setUsableSize(tryAsset.getUsableSize() + request.amount());
        assetService.save(tryAsset);

        recordTransaction(customer, request.amount(), TransactionType.DEPOSIT);
        return true;
    }

    @Transactional
    public boolean withdraw(WithdrawRequest request) {
        Customer customer = customerService.getById(request.customerId());

        if (customer.getIban() == null) {
            throw new IllegalStateException("User should have an IBAN");
        }

        Asset tryAsset = assetService.getAssetByCustomerIdAndName(request.customerId(), "TRY");
        if (tryAsset.getUsableSize() < request.amount()) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        tryAsset.setSize(tryAsset.getSize() - request.amount());
        tryAsset.setUsableSize(tryAsset.getUsableSize() - request.amount());
        assetService.save(tryAsset);

        recordTransaction(customer, request.amount(), TransactionType.WITHDRAW);
        return true;
    }

    private void recordTransaction(Customer customer, int amount, TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setCustomer(customer);
        transaction.setAmount(amount);
        transactionRepository.save(transaction);
    }
}
