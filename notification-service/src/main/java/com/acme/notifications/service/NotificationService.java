package com.acme.notifications.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.acme.common.error.ResourceNotFoundException;
import com.acme.notifications.web.NotificationRequest;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Map<String, String> templates = new HashMap<>();
    private final Map<String, Notification> sent = new ConcurrentHashMap<>();

    @PostConstruct
    void registerTemplates() {
        templates.put("ORDER_CONFIRMED", "Thanks for your order #${orderId}! Total: ${total}.");
        templates.put("ORDER_CANCELLED", "Order #${orderId} has been cancelled.");
        log.info("Registered {} notification templates", templates.size());
    }

    public Notification send(NotificationRequest request) {
        String template = templates.get(request.getTemplate());
        if (template == null) {
            throw new ResourceNotFoundException("Template", request.getTemplate());
        }
        String body = new StrSubstitutor(request.getVariables()).replace(template);
        Notification notification = new Notification(request.getRecipient(), request.getChannel(), request.getTemplate(), body);
        sent.put(notification.getId(), notification);
        log.info("Dispatched {} to {} via {}", request.getTemplate(), request.getRecipient(), request.getChannel());
        return notification;
    }

    public Notification get(String id) {
        Notification notification = sent.get(id);
        if (notification == null) {
            throw new ResourceNotFoundException("Notification", id);
        }
        return notification;
    }

    @PreAuthorize("hasRole('SUPPORT')")
    public List<Notification> findByRecipient(String recipient) {
        return sent.values().stream()
                .filter(n -> n.getRecipient().equalsIgnoreCase(recipient))
                .collect(Collectors.toList());
    }
}
