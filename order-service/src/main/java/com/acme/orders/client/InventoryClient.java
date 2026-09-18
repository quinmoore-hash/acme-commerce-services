package com.acme.orders.client;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import com.acme.orders.config.DownstreamProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public InventoryClient(RestTemplate restTemplate, DownstreamProperties properties) {
        this.restTemplate = restTemplate;
        this.baseUrl = properties.getInventoryBaseUrl();
    }

    public StockReservation reserve(String sku, int quantity) {
        Map<String, Integer> body = Collections.singletonMap("quantity", quantity);
        try {
            ResponseEntity<StockReservation> response = restTemplate.postForEntity(
                    baseUrl + "/api/inventory/{sku}/reserve", body, StockReservation.class, sku);
            if (response.getStatusCodeValue() != HttpStatus.OK.value() || response.getBody() == null) {
                throw new IllegalStateException("Unexpected inventory response for " + sku + ": "
                        + response.getStatusCodeValue());
            }
            return response.getBody();
        } catch (HttpClientErrorException.Conflict ex) {
            throw new IllegalStateException("Insufficient stock for SKU " + sku);
        }
    }

    public void release(String sku, int quantity) {
        try {
            restTemplate.postForEntity(baseUrl + "/api/inventory/{sku}/release",
                    Collections.singletonMap("quantity", quantity), Void.class, sku);
        } catch (RuntimeException ex) {
            log.warn("Failed to release {} x {}: {}", quantity, sku, ex.getMessage());
        }
    }

    public static class StockReservation {
        private String sku;
        private int quantity;
        private BigDecimal unitPrice;
        private String currency;

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}
