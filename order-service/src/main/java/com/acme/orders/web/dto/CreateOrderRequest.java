package com.acme.orders.web.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class CreateOrderRequest {

    @NotBlank
    @Email
    private String customerEmail;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency = "USD";

    @NotEmpty
    @Valid
    private List<OrderLineRequest> lines = new ArrayList<>();

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<OrderLineRequest> getLines() {
        return lines;
    }

    public void setLines(List<OrderLineRequest> lines) {
        this.lines = lines;
    }
}
