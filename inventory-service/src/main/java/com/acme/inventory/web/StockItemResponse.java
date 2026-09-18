package com.acme.inventory.web;

import java.math.BigDecimal;

import com.acme.inventory.domain.StockItem;

public class StockItemResponse {

    private String sku;
    private String productName;
    private int quantity;
    private int quantityOnHand;
    private int quantityReserved;
    private int available;
    private BigDecimal unitPrice;
    private String currency;

    public static StockItemResponse from(StockItem item) {
        StockItemResponse response = new StockItemResponse();
        response.sku = item.getSku();
        response.productName = item.getProductName();
        response.quantityOnHand = item.getQuantityOnHand();
        response.quantityReserved = item.getQuantityReserved();
        response.available = item.available();
        response.unitPrice = item.getUnitPrice();
        response.currency = item.getCurrency();
        return response;
    }

    public StockItemResponse withQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public int getQuantityReserved() {
        return quantityReserved;
    }

    public int getAvailable() {
        return available;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getCurrency() {
        return currency;
    }
}
