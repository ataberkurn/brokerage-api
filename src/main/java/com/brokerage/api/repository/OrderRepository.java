package com.brokerage.api.repository;

import com.brokerage.api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByCustomer_IdAndCreatedAtBetween(UUID customerId, LocalDateTime startDate, LocalDateTime endDate);
}
