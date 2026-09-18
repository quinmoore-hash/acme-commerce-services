package com.acme.orders.client;

import java.util.HashMap;
import java.util.Map;

import com.acme.orders.config.DownstreamProperties;
import com.acme.orders.domain.Order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NotificationClient(RestTemplate restTemplate, DownstreamProperties properties) {
        this.restTemplate = restTemplate;
        this.baseUrl = properties.getNotificationBaseUrl();
    }

    public void orderConfirmed(Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("recipient", order.getCustomerEmail());
        payload.put("channel", "EMAIL");
        payload.put("template", "ORDER_CONFIRMED");
        Map<String, String> variables = new HashMap<>();
        variables.put("orderId", String.valueOf(order.getId()));
        variables.put("total", order.getTotal() + " " + order.getCurrency());
        payload.put("variables", variables);

        try {
            restTemplate.postForLocation(baseUrl + "/api/notifications", payload);
        } catch (RestClientException ex) {
            log.warn("Notification for order {} could not be sent: {}", order.getId(), ex.getMessage());
        }
    }
}
