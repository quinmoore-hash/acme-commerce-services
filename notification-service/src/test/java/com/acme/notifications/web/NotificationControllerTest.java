package com.acme.notifications.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    private static final String ORDER_CONFIRMED = "{\"recipient\":\"jane@example.com\",\"channel\":\"EMAIL\","
            + "\"template\":\"ORDER_CONFIRMED\",\"variables\":{\"orderId\":\"42\",\"total\":\"50.00 USD\"}}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersTemplateAndStoresNotification() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .with(httpBasic("order-service", "order-service"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ORDER_CONFIRMED))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.body").value("Thanks for your order #42! Total: 50.00 USD."));
    }

    @Test
    void rejectsUnknownTemplate() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .with(httpBasic("order-service", "order-service"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"jane@example.com\",\"template\":\"NOPE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Template not found: NOPE"));
    }

    @Test
    void validatesPayload() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .with(httpBasic("order-service", "order-service"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"nope\",\"channel\":\"FAX\",\"template\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.length()").value(3));
    }

    @Test
    void recipientLookupRequiresSupportRole() throws Exception {
        mockMvc.perform(get("/api/notifications").param("recipient", "jane@example.com")
                        .with(httpBasic("order-service", "order-service")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/notifications").param("recipient", "jane@example.com")
                        .with(httpBasic("support", "support")))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mockMvc.perform(get("/api/notifications").param("recipient", "jane@example.com"))
                .andExpect(status().isUnauthorized());
    }
}
