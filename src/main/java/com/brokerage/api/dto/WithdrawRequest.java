package com.brokerage.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawRequest(UUID customerId, BigDecimal amount, String iban) {
}
