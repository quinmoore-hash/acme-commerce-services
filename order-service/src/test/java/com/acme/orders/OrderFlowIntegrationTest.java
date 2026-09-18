package com.acme.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.Collections;

import com.acme.orders.client.InventoryClient;
import com.acme.orders.client.InventoryClient.StockReservation;
import com.acme.orders.client.NotificationClient;
import com.acme.orders.web.dto.CreateOrderRequest;
import com.acme.orders.web.dto.OrderLineRequest;
import com.acme.orders.web.dto.OrderResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private NotificationClient notificationClient;

    @Test
    void placesAndCancelsOrderEndToEnd() {
        given(inventoryClient.reserve(anyString(), anyInt())).willAnswer(invocation -> {
            StockReservation reservation = new StockReservation();
            reservation.setSku(invocation.getArgument(0));
            reservation.setQuantity(invocation.getArgument(1));
            reservation.setUnitPrice(new BigDecimal("12.50"));
            reservation.setCurrency("USD");
            return reservation;
        });

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerEmail("it@example.com");
        request.setLines(Collections.singletonList(new OrderLineRequest("SKU-IT", 4)));

        ResponseEntity<OrderResponse> created = rest.withBasicAuth("customer", "customer")
                .postForEntity("http://localhost:" + port + "/api/orders", request, OrderResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getHeaders().getLocation()).isNotNull();
        assertThat(created.getBody().getTotal()).isEqualByComparingTo("50.00");

        ResponseEntity<OrderResponse> fetched = rest.withBasicAuth("customer", "customer")
                .getForEntity(created.getHeaders().getLocation(), OrderResponse.class);
        assertThat(fetched.getStatusCodeValue()).isEqualTo(200);
        assertThat(fetched.getBody().getStatus().name()).isEqualTo("CONFIRMED");

        ResponseEntity<OrderResponse> cancelled = rest.withBasicAuth("admin", "admin")
                .exchange(created.getHeaders().getLocation(), org.springframework.http.HttpMethod.DELETE, null, OrderResponse.class);
        assertThat(cancelled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelled.getBody().getStatus().name()).isEqualTo("CANCELLED");
    }

    @Test
    void actuatorHealthIsPublic() {
        ResponseEntity<String> health = rest.getForEntity("http://localhost:" + port + "/actuator/health", String.class);
        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(health.getBody()).contains("UP");
    }
}
