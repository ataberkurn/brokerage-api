package com.brokerage.api.controller;

import com.brokerage.api.dto.OrderRequest;
import com.brokerage.api.entity.Order;
import com.brokerage.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public boolean create(OrderRequest request) {
        return orderService.create(request);
    }

    @PostMapping("/{orderId}/match")
    public boolean match(@PathVariable UUID orderId) {
        return orderService.match(orderId);
    }

    @GetMapping
    public List<Order> getOrdersByCustomerId(@RequestParam UUID customerId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return orderService.getOrdersByCustomerId(customerId, startDate, endDate);
    }

    @DeleteMapping("/{orderId}")
    public boolean delete(@PathVariable UUID orderId) {
        return orderService.delete(orderId);
    }
}
