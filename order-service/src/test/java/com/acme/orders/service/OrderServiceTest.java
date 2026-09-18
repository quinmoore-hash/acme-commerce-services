package com.acme.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;

import com.acme.orders.client.InventoryClient;
import com.acme.orders.client.InventoryClient.StockReservation;
import com.acme.orders.client.NotificationClient;
import com.acme.orders.config.DownstreamProperties;
import com.acme.orders.domain.Order;
import com.acme.orders.domain.OrderStatus;
import com.acme.orders.repository.OrderRepository;
import com.acme.orders.web.dto.CreateOrderRequest;
import com.acme.orders.web.dto.OrderLineRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private NotificationClient notificationClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        DownstreamProperties props = new DownstreamProperties("http://inventory", "http://notifications",
                Duration.ofSeconds(1), Duration.ofSeconds(1));
        orderService = new OrderService(orderRepository, inventoryClient, notificationClient, props);
    }

    @Test
    void placesOrderAndNotifies() {
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(inventoryClient.reserve("SKU-A", 2)).willReturn(reservation("SKU-A", 2, "10.00"));
        given(inventoryClient.reserve("SKU-B", 1)).willReturn(reservation("SKU-B", 1, "4.25"));

        Order order = orderService.placeOrder(request("SKU-A", 2, "SKU-B", 1));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getTotal()).isEqualByComparingTo(new BigDecimal("24.25"));
        assertThat(order.getLines()).hasSize(2);
        verify(notificationClient).orderConfirmed(order);
    }

    @Test
    void releasesReservationsWhenLaterLineFails() {
        given(inventoryClient.reserve("SKU-A", 2)).willReturn(reservation("SKU-A", 2, "10.00"));
        given(inventoryClient.reserve("SKU-B", 1)).willThrow(new IllegalStateException("Insufficient stock for SKU SKU-B"));

        assertThatThrownBy(() -> orderService.placeOrder(request("SKU-A", 2, "SKU-B", 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SKU-B");

        verify(inventoryClient).release("SKU-A", 2);
        verify(orderRepository, never()).save(any());
        verify(notificationClient, never()).orderConfirmed(any());
    }

    private static StockReservation reservation(String sku, int quantity, String price) {
        StockReservation reservation = new StockReservation();
        reservation.setSku(sku);
        reservation.setQuantity(quantity);
        reservation.setUnitPrice(new BigDecimal(price));
        reservation.setCurrency("USD");
        return reservation;
    }

    private static CreateOrderRequest request(String sku1, int qty1, String sku2, int qty2) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerEmail("jane@example.com");
        request.setCurrency("USD");
        request.setLines(Arrays.asList(new OrderLineRequest(sku1, qty1), new OrderLineRequest(sku2, qty2)));
        return request;
    }
}
