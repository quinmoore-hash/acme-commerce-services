package com.acme.orders.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.acme.common.error.ResourceNotFoundException;
import com.acme.orders.client.InventoryClient;
import com.acme.orders.client.InventoryClient.StockReservation;
import com.acme.orders.client.NotificationClient;
import com.acme.orders.config.DownstreamProperties;
import com.acme.orders.domain.Order;
import com.acme.orders.domain.OrderLine;
import com.acme.orders.domain.OrderStatus;
import com.acme.orders.repository.OrderRepository;
import com.acme.orders.web.dto.CreateOrderRequest;
import com.acme.orders.web.dto.OrderLineRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final NotificationClient notificationClient;
    private final DownstreamProperties downstream;

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient,
            NotificationClient notificationClient, DownstreamProperties downstream) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.notificationClient = notificationClient;
        this.downstream = downstream;
    }

    @PostConstruct
    void logDownstreams() {
        log.info("Order service wired to inventory={} notifications={}",
                downstream.getInventoryBaseUrl(), downstream.getNotificationBaseUrl());
    }

    @Transactional
    public Order placeOrder(CreateOrderRequest request) {
        Order order = new Order(request.getCustomerEmail(), request.getCurrency());
        List<StockReservation> reservations = new ArrayList<>();
        try {
            for (OrderLineRequest lineRequest : request.getLines()) {
                StockReservation reservation = inventoryClient.reserve(lineRequest.getSku(), lineRequest.getQuantity());
                reservations.add(reservation);
                order.addLine(new OrderLine(reservation.getSku(), reservation.getQuantity(), reservation.getUnitPrice()));
            }
        } catch (RuntimeException ex) {
            reservations.forEach(r -> inventoryClient.release(r.getSku(), r.getQuantity()));
            throw ex;
        }

        order.confirm();
        Order saved = orderRepository.save(order);
        notificationClient.orderConfirmed(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Transactional(readOnly = true)
    public List<Order> findOrders(String customerEmail, OrderStatus status) {
        if (customerEmail != null) {
            return orderRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(customerEmail);
        }
        if (status != null) {
            return orderRepository.findByStatus(status);
        }
        return orderRepository.findAll();
    }

    @Transactional
    public Order cancelOrder(Long id) {
        Order order = getOrder(id);
        order.cancel();
        order.getLines().forEach(line -> inventoryClient.release(line.getSku(), line.getQuantity()));
        return orderRepository.save(order);
    }
}
