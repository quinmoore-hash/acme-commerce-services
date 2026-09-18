package com.acme.orders.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OrderLineRequest {

    @NotBlank
    @Pattern(regexp = "[A-Z0-9-]{3,32}", message = "must be an upper-case SKU")
    private String sku;

    @Min(1)
    @Max(100)
    private int quantity;

    public OrderLineRequest() {
    }

    public OrderLineRequest(String sku, int quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

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
}
