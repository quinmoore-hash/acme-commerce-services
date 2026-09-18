package com.acme.inventory.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "stock_items", uniqueConstraints = @UniqueConstraint(columnNames = "sku"))
@EntityListeners(AuditingEntityListener.class)
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 32)
    private String sku;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantityOnHand;

    @Column(nullable = false)
    private int quantityReserved;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private long version;

    protected StockItem() {
    }

    public StockItem(String sku, String productName, int quantityOnHand, BigDecimal unitPrice, String currency) {
        this.sku = sku;
        this.productName = productName;
        this.quantityOnHand = quantityOnHand;
        this.unitPrice = unitPrice;
        this.currency = currency;
    }

    public int available() {
        return quantityOnHand - quantityReserved;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (available() < quantity) {
            throw new IllegalStateException("Insufficient stock for " + sku + ": requested " + quantity
                    + ", available " + available());
        }
        quantityReserved += quantity;
    }

    public void release(int quantity) {
        quantityReserved = Math.max(0, quantityReserved - quantity);
    }

    public void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        quantityOnHand += quantity;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public int getQuantityReserved() {
        return quantityReserved;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
