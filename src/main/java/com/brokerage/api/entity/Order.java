package com.brokerage.api.entity;

import com.brokerage.api.enumeration.OrderSide;
import com.brokerage.api.enumeration.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    private String assetName;
    @Enumerated(EnumType.STRING)
    private OrderSide side;
    private int size;
    private int price;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
