package com.brokerage.api.dto;

import java.util.UUID;

public record WithdrawRequest(UUID customerId, int amount, String iban) {
}
