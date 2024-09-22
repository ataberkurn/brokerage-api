package com.brokerage.api.service;

import com.brokerage.api.entity.Customer;
import com.brokerage.api.enumeration.Role;
import com.brokerage.api.exception.ResourceNotFoundException;
import com.brokerage.api.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private UUID customerId;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        customerId = UUID.randomUUID();
        customer = new Customer();
        customer.setId(customerId);
        customer.setName("Ataberk");
    }

    @Test
    public void testCreate_ValidCustomer() {
        boolean result = customerService.create(customer);

        assertTrue(result);
        assertEquals(Role.CUSTOMER, customer.getRole());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    public void testGetById_CustomerExists() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Customer foundCustomer = customerService.getById(customerId);

        assertNotNull(foundCustomer);
        assertEquals(customerId, foundCustomer.getId());
    }

    @Test
    public void testGetById_CustomerNotFound() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> customerService.getById(customerId));
        assertEquals("Customer not found with ID: '" + customerId + "'", exception.getMessage());
    }
}
