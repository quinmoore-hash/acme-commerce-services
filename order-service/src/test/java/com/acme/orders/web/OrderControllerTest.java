package com.acme.orders.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;

import com.acme.common.error.ResourceNotFoundException;
import com.acme.orders.config.SecurityConfig;
import com.acme.orders.domain.Order;
import com.acme.orders.domain.OrderLine;
import com.acme.orders.service.OrderService;
import com.acme.orders.web.dto.CreateOrderRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void rejectsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void returnsOrder() throws Exception {
        Order order = new Order("jane@example.com", "USD");
        order.addLine(new OrderLine("SKU-1", 2, new BigDecimal("10.00")));
        given(orderService.getOrder(1L)).willReturn(order);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerEmail").value("jane@example.com"))
                .andExpect(jsonPath("$.total").value(20.00))
                .andExpect(jsonPath("$.lines[0].sku").value("SKU-1"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listsOrdersWithTrailingSlash() throws Exception {
        given(orderService.findOrders(null, null)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/orders/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void mapsNotFoundToApiError() throws Exception {
        given(orderService.getOrder(42L)).willThrow(new ResourceNotFoundException("Order", 42L));

        mockMvc.perform(get("/api/orders/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found: 42"))
                .andExpect(jsonPath("$.path").value("/api/orders/42"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void validatesRequestBody() throws Exception {
        String body = "{\"customerEmail\":\"not-an-email\",\"currency\":\"USD\",\"lines\":[{\"sku\":\"bad sku\",\"quantity\":0}]}";

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.length()").value(3));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createsOrder() throws Exception {
        Order order = new Order("jane@example.com", "USD");
        order.addLine(new OrderLine("SKU-1", 1, new BigDecimal("5.50")));
        given(orderService.placeOrder(any(CreateOrderRequest.class))).willReturn(order);
        String body = "{\"customerEmail\":\"jane@example.com\",\"currency\":\"USD\",\"lines\":[{\"sku\":\"SKU-1\",\"quantity\":1}]}";

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void cancellationRequiresAdmin() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/orders/1"))
                .andExpect(status().isForbidden());
    }
}
