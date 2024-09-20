package com.brokerage.api.entity;

import jakarta.persistence.Entity;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Customer extends User {

    private BigDecimal balance;
    private String iban;
}
