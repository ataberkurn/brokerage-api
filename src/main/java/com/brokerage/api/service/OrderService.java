package com.brokerage.api.service;

import com.brokerage.api.dto.OrderRequest;
import com.brokerage.api.entity.Asset;
import com.brokerage.api.entity.Order;
import com.brokerage.api.enumeration.OrderSide;
import com.brokerage.api.enumeration.OrderStatus;
import com.brokerage.api.exception.ResourceNotFoundException;
import com.brokerage.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final CustomerService customerService;
    private final AssetService assetService;
    private final AssetValidationService assetValidationService;

    @Transactional
    public boolean create(OrderRequest request) {
        validateRequest(request);

        int totalCost = request.price() * request.size();
        Asset tryAsset = assetService.getAssetByCustomerIdAndName(request.customerId(), "TRY");
        Asset requestedAsset = getOrCreateRequestedAsset(request);

        processOrder(request, totalCost, tryAsset, requestedAsset);

        assetService.save(tryAsset);
        assetService.save(requestedAsset);

        recordOrder(request);
        return true;
    }

    private void validateRequest(OrderRequest request) {
        if (!assetValidationService.isAssetValid(request.assetName())) {
            throw new IllegalArgumentException("Asset not valid");
        }

        if (!userService.userExists(request.customerId())) {
            throw new ResourceNotFoundException("Customer", "ID", request.customerId());
        }
    }

    private Asset getOrCreateRequestedAsset(OrderRequest request) {
        Asset requestedAsset = assetService.getAssetByCustomerIdAndName(request.customerId(), request.assetName());
        if (requestedAsset == null) {
            requestedAsset = new Asset();
            requestedAsset.setCustomer(customerService.getById(request.customerId()));
            requestedAsset.setName(request.assetName());
            requestedAsset.setSize(0);
            requestedAsset.setUsableSize(0);
        }
        return requestedAsset;
    }

    private void processOrder(OrderRequest request, int totalCost, Asset tryAsset, Asset requestedAsset) {
        if (request.side() == OrderSide.BUY) {
            if (tryAsset.getUsableSize() < totalCost) {
                throw new IllegalArgumentException("not enough usable size in TRY asset.");
            }

            tryAsset.setUsableSize(tryAsset.getUsableSize() - totalCost);
            requestedAsset.setUsableSize(requestedAsset.getUsableSize() + request.size());
        } else if (request.side() == OrderSide.SELL) {
            if (requestedAsset.getUsableSize() < request.size()) {
                throw new IllegalArgumentException("not enough usable size in your requested asset.");
            }

            tryAsset.setUsableSize(tryAsset.getUsableSize() + totalCost);
            requestedAsset.setUsableSize(requestedAsset.getUsableSize() - request.size());
        }
    }

    @CachePut(value = "orders", key = "#result.id", cacheManager = "redisCacheManager")
    public Order recordOrder(OrderRequest request) {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setCustomer(customerService.getById(request.customerId()));
        order.setAssetName(request.assetName());
        order.setSize(request.size());
        order.setPrice(request.price());
        order.setSide(request.side());
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    @Transactional
    public boolean match(UUID orderId) {
        Order order = getById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be matched.");
        }

        if (order.getSide() == OrderSide.BUY) {
            Asset asset = assetService.getAssetByCustomerIdAndName(order.getCustomer().getId(), order.getAssetName());
            asset.setSize(asset.getSize() + order.getSize());
            assetService.save(asset);
        } else if (order.getSide() == OrderSide.SELL) {
            Asset tryAsset = assetService.getAssetByCustomerIdAndName(order.getCustomer().getId(), "TRY");
            tryAsset.setSize(tryAsset.getSize() + (order.getSize() * order.getPrice()));
            assetService.save(tryAsset);
        }

        updateStatus(order, OrderStatus.MATCHED);
        return true;
    }

    @Cacheable(value = "orders", key = "#customerId + #startDate + #endDate", cacheManager = "redisCacheManager")
    public List<Order> getOrdersByCustomerId(UUID customerId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        return orderRepository.findAllByCustomer_IdAndCreatedAtBetween(customerId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59, 999999999));
    }

    @Cacheable(value = "orders", key = "#id", cacheManager = "redisCacheManager")
    public Order getById(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", "ID", id));
    }

    @Transactional
    public boolean delete(UUID orderId) {
        Order order = getById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order cannot be cancelled");
        }

        int amountToReturn = order.getPrice() * order.getSize();

        Asset tryAsset = assetService.getAssetByCustomerIdAndName(order.getCustomer().getId(), "TRY");
        tryAsset.setUsableSize(tryAsset.getUsableSize() + amountToReturn);
        assetService.save(tryAsset);

        updateStatus(order, OrderStatus.CANCELLED);
        return true;
    }

    @CachePut(value = "orders", key = "#order.id", cacheManager = "redisCacheManager")
    public Order updateStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
