package com.brokerage.api.dto;

import com.brokerage.api.enumeration.OrderSide;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderRequest(UUID customerId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) { }
