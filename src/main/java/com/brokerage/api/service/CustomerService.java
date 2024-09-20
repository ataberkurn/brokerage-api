package com.brokerage.api.service;

import com.brokerage.api.entity.Customer;
import com.brokerage.api.exception.CustomerNotFoundException;
import com.brokerage.api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public void save(Customer customer) {
        customerRepository.save(customer);
    }

    public Customer getById(UUID id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException("customer not found with ID: " + id));
    }
}
