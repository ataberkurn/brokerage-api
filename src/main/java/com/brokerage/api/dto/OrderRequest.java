package com.brokerage.api.dto;

import com.brokerage.api.enumeration.OrderSide;

import java.util.UUID;

public record OrderRequest(UUID customerId, String assetName, OrderSide side, int size, int price) { }
