package com.brokerage.api.service;

import com.brokerage.api.dto.OrderRequest;
import com.brokerage.api.entity.Asset;
import com.brokerage.api.entity.Customer;
import com.brokerage.api.entity.Order;
import com.brokerage.api.enumeration.OrderSide;
import com.brokerage.api.enumeration.OrderStatus;
import com.brokerage.api.exception.ResourceNotFoundException;
import com.brokerage.api.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserService userService;
    @Mock
    private AssetService assetService;
    @Mock
    private CustomerService customerService;
    @Mock
    private AssetValidationService assetValidationService;
    @InjectMocks
    private OrderService orderService;

    private final int NUM_THREADS = 10;
    private final ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
    private OrderRequest orderRequest;
    private UUID customerId;
    private Asset tryAsset;
    private Asset requestedAsset;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        customerId = UUID.randomUUID();
        orderRequest = new OrderRequest(customerId, "ABC", OrderSide.BUY, 10, 100);

        tryAsset = new Asset();
        tryAsset.setName("TRY");
        tryAsset.setSize(1000);
        tryAsset.setUsableSize(1000);

        requestedAsset = new Asset();
        requestedAsset.setName("ABC");
        requestedAsset.setSize(0);
        requestedAsset.setUsableSize(0);
    }

    @Test
    public void testCreate_ValidRequest() {
        when(assetValidationService.isAssetValid(orderRequest.assetName())).thenReturn(true);
        when(userService.userExists(customerId)).thenReturn(true);
        when(assetService.getAssetByCustomerIdAndName(customerId, "TRY")).thenReturn(tryAsset);
        when(assetService.getAssetByCustomerIdAndName(customerId, orderRequest.assetName())).thenReturn(requestedAsset);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());

        boolean result = orderService.create(orderRequest);

        assertTrue(result);
        verify(assetService, times(1)).save(tryAsset);
        verify(assetService, times(1)).save(requestedAsset);
    }

    @Test
    public void testCreate_InvalidAsset() {
        when(assetValidationService.isAssetValid(orderRequest.assetName())).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> orderService.create(orderRequest));
        assertEquals("Asset not valid", exception.getMessage());
    }

    @Test
    public void testCreate_NonExistentUser() {
        when(assetValidationService.isAssetValid(orderRequest.assetName())).thenReturn(true);
        when(userService.userExists(customerId)).thenReturn(false);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> orderService.create(orderRequest));
        assertEquals("Customer not found with ID: '" + customerId + "'", exception.getMessage());
    }

    @Test
    public void testCreate_ConcurrentOrders() throws InterruptedException, ExecutionException {
        OrderRequest request = new OrderRequest(customerId, "ABC", OrderSide.BUY, 1, 100);
        Asset sharedAsset = new Asset();
        sharedAsset.setUsableSize(100);

        when(userService.userExists(customerId)).thenReturn(true);
        when(assetValidationService.isAssetValid(request.assetName())).thenReturn(true);

        when(assetService.getAssetByCustomerIdAndName(customerId, "TRY")).thenReturn(tryAsset);
        when(assetService.getAssetByCustomerIdAndName(customerId, request.assetName())).thenReturn(sharedAsset);

        CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < NUM_THREADS; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    return orderService.create(request);
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();

        for (Future<Boolean> future : futures) {
            assertTrue(future.get(), "Order creation failed");
        }

        verify(orderRepository, times(NUM_THREADS)).save(any());
    }

    @Test
    public void testGetOrdersByCustomerId() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        when(orderRepository.findAllByCustomer_IdAndCreatedAtBetween(customerId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59, 999999999)))
                .thenReturn(List.of(new Order()));

        List<Order> orders = orderService.getOrdersByCustomerId(customerId, startDate, endDate);

        assertEquals(1, orders.size());
        verify(orderRepository, times(1)).findAllByCustomer_IdAndCreatedAtBetween(customerId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59, 999999999));
    }

    @Test
    public void testGetOrdersByCustomerId_InvalidDate() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().minusDays(1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> orderService.getOrdersByCustomerId(customerId, startDate, endDate));
        assertEquals("Start date must be before or equal to end date", exception.getMessage());
    }

    @Test
    public void testGetById_OrderExists() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Order foundOrder = orderService.getById(orderId);

        assertNotNull(foundOrder);
        assertEquals(orderId, foundOrder.getId());
    }

    @Test
    public void testGetById_OrderNotFound() {
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> orderService.getById(orderId));
        assertEquals("Order not found with ID: '" + orderId + "'", exception.getMessage());
    }

    @Test
    public void testMatch_ValidOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setSide(OrderSide.BUY);
        Customer customer = new Customer();
        customer.setId(customerId);
        order.setCustomer(customer);
        order.setSize(10);
        order.setAssetName("ABC");
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetService.getAssetByCustomerIdAndName(customerId, "ABC")).thenReturn(requestedAsset);
        when(assetService.save(any(Asset.class))).thenReturn(requestedAsset);

        boolean result = orderService.match(orderId);

        assertTrue(result);
        assertEquals(10, requestedAsset.getSize());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testMatch_OrderNotPending() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.MATCHED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Exception exception = assertThrows(IllegalStateException.class, () -> orderService.match(orderId));
        assertEquals("Only pending orders can be matched.", exception.getMessage());
    }

    @Test
    public void testDelete_ValidOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING);
        order.setSize(10);
        order.setPrice(100);
        Customer customer = new Customer();
        customer.setId(customerId);
        order.setCustomer(customer);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assetService.getAssetByCustomerIdAndName(customerId, "TRY")).thenReturn(tryAsset);

        boolean result = orderService.delete(orderId);

        assertTrue(result);
        assertEquals(2000, tryAsset.getUsableSize());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testDelete_OrderNotPending() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Exception exception = assertThrows(IllegalStateException.class, () -> orderService.delete(orderId));
        assertEquals("Order cannot be cancelled", exception.getMessage());
    }

    @AfterEach
    public void tearDown() {
        executorService.shutdown();
    }
}
