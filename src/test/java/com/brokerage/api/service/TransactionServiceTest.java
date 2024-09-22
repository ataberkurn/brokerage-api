package com.brokerage.api.service;

import com.brokerage.api.dto.DepositRequest;
import com.brokerage.api.dto.WithdrawRequest;
import com.brokerage.api.entity.Asset;
import com.brokerage.api.entity.Customer;
import com.brokerage.api.entity.Transaction;
import com.brokerage.api.exception.InsufficientBalanceException;
import com.brokerage.api.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;  

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private AssetService assetService;
    @InjectMocks
    private TransactionService transactionService;

    private DepositRequest depositRequest;
    private WithdrawRequest withdrawRequest;
    private Customer customer;
    private Asset tryAsset;
    private UUID customerId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        customerId = UUID.randomUUID();
        customer = new Customer();
        customer.setId(customerId);
        customer.setIban("123456");

        depositRequest = new DepositRequest(customerId, BigDecimal.valueOf(100));
        withdrawRequest = new WithdrawRequest(customerId, BigDecimal.valueOf(50), "123456");

        tryAsset = new Asset();
        tryAsset.setSize(BigDecimal.valueOf(200));
        tryAsset.setUsableSize(BigDecimal.valueOf(200));
    }

    @Test
    public void testDeposit_ValidRequest() {
        when(customerService.getById(customerId)).thenReturn(customer);
        when(assetService.getAssetByCustomerIdAndName(customerId, "TRY")).thenReturn(tryAsset);

        boolean result = transactionService.deposit(depositRequest);

        assertTrue(result);
        assertEquals(BigDecimal.valueOf(300), tryAsset.getSize());
        assertEquals(BigDecimal.valueOf(300), tryAsset.getUsableSize());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testDeposit_NegativeAmount() {
        depositRequest = new DepositRequest(customerId, BigDecimal.valueOf(-50));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> transactionService.deposit(depositRequest));
        assertEquals("Deposit amount must be positive.", exception.getMessage());
    }

    @Test
    public void testWithdraw_ValidRequest() {
        when(customerService.getById(customerId)).thenReturn(customer);
        when(assetService.getAssetByCustomerIdAndName(customerId, "TRY")).thenReturn(tryAsset);

        boolean result = transactionService.withdraw(withdrawRequest);

        assertTrue(result);
        assertEquals(BigDecimal.valueOf(150), tryAsset.getSize());
        assertEquals(BigDecimal.valueOf(150), tryAsset.getUsableSize());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testWithdraw_InsufficientBalance() {
        withdrawRequest = new WithdrawRequest(customerId, BigDecimal.valueOf(250), "");
        when(customerService.getById(customerId)).thenReturn(customer);
        when(assetService.getAssetByCustomerIdAndName(customerId, "TRY")).thenReturn(tryAsset);

        Exception exception = assertThrows(InsufficientBalanceException.class, () -> transactionService.withdraw(withdrawRequest));
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    public void testWithdraw_NoIban() {
        customer.setIban(null);
        when(customerService.getById(customerId)).thenReturn(customer);

        Exception exception = assertThrows(IllegalStateException.class, () -> transactionService.withdraw(withdrawRequest));
        assertEquals("User should have an IBAN", exception.getMessage());
    }
}