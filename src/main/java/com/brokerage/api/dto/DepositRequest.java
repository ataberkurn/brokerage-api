package com.brokerage.api.dto;

import java.util.UUID;

public record DepositRequest(UUID customerId, int amount) {
}
