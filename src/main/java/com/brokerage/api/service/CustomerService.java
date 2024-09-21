package com.brokerage.api.service;

import com.brokerage.api.entity.Customer;
import com.brokerage.api.enumeration.Role;
import com.brokerage.api.exception.CustomerNotFoundException;
import com.brokerage.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @CachePut(value = "customers", key = "#customer.id", cacheManager = "redisCacheManager")
    public boolean create(Customer customer) {
        try {
            customer.setCreatedAt(LocalDateTime.now());
            customer.setRole(Role.CUSTOMER);
            customerRepository.save(customer);
            return true;
        } catch (DataAccessException exception) {
            return false;
        }
    }

    @Cacheable(value = "customers", key = "#id", cacheManager = "redisCacheManager")
    public Customer getById(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException("customer not found with ID: " + id));
    }
}
