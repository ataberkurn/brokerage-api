package com.brokerage.api.entity;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Customer extends User {

    private String iban;
}
