package com.acme.inventory.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.acme.inventory.domain.StockItem;
import com.acme.inventory.repository.StockItemRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StockItemRepository repository;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.save(new StockItem("SKU-TEST", "Test widget", 5, new BigDecimal("2.50"), "USD"));
    }

    @Test
    void readsArePublic() throws Exception {
        mockMvc.perform(get("/api/inventory/SKU-TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(5))
                .andExpect(jsonPath("$.unitPrice").value(2.50));
    }

    @Test
    void unknownSkuIsNotFound() throws Exception {
        mockMvc.perform(get("/api/inventory/NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("StockItem not found: NOPE"));
    }

    @Test
    void reserveRequiresServiceCredentials() throws Exception {
        mockMvc.perform(post("/api/inventory/SKU-TEST/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reservesAndReleasesStock() throws Exception {
        mockMvc.perform(post("/api/inventory/SKU-TEST/reserve")
                        .with(httpBasic("order-service", "order-service"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.available").value(2));

        mockMvc.perform(post("/api/inventory/SKU-TEST/reserve")
                        .with(httpBasic("order-service", "order-service"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient stock for SKU-TEST: requested 3, available 2"));

        mockMvc.perform(post("/api/inventory/SKU-TEST/release")
                        .with(httpBasic("order-service", "order-service"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/inventory/SKU-TEST"))
                .andExpect(jsonPath("$.available").value(5));
    }

    @Test
    void restockRequiresAdminRole() throws Exception {
        String body = "{\"productName\":\"Test widget\",\"quantity\":10,\"unitPrice\":2.50,\"currency\":\"USD\"}";

        mockMvc.perform(put("/api/inventory/SKU-TEST")
                        .with(httpBasic("order-service", "order-service"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/inventory/SKU-TEST")
                        .with(httpBasic("warehouse", "warehouse"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityOnHand").value(15));
    }

    @Test
    void validatesQuantity() throws Exception {
        mockMvc.perform(post("/api/inventory/SKU-TEST/reserve")
                        .with(httpBasic("order-service", "order-service"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value("quantity: must be greater than or equal to 1"));
    }
}
