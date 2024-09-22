package com.brokerage.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Customer extends User {

    @Column(length = 34)
    private String iban;
}
