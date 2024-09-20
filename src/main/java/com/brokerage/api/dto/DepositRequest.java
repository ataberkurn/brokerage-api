package com.brokerage.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositRequest(UUID customerId, BigDecimal amount) {
}
