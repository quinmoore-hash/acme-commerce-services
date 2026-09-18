package com.acme.orders.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.acme.orders.domain.Order;
import com.acme.orders.domain.OrderStatus;

public class OrderResponse {

    private Long id;
    private String customerEmail;
    private OrderStatus status;
    private String currency;
    private BigDecimal total;
    private List<Line> lines;
    private Instant createdAt;

    public static OrderResponse from(Order order) {
        OrderResponse response = new OrderResponse();
        response.id = order.getId();
        response.customerEmail = order.getCustomerEmail();
        response.status = order.getStatus();
        response.currency = order.getCurrency();
        response.total = order.getTotal();
        response.createdAt = order.getCreatedAt();
        response.lines = order.getLines().stream()
                .map(line -> new Line(line.getSku(), line.getQuantity(), line.getUnitPrice(), line.lineTotal()))
                .collect(Collectors.toList());
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<Line> getLines() {
        return lines;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public static class Line {
        private final String sku;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal lineTotal;

        public Line(String sku, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
            this.sku = sku;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }

        public String getSku() {
            return sku;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }
    }
}
