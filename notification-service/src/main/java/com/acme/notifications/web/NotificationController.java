package com.acme.notifications.web;

import java.net.URI;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.acme.notifications.service.Notification;
import com.acme.notifications.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> send(@Valid @RequestBody NotificationRequest request,
            HttpServletRequest httpRequest) {
        Notification notification = notificationService.send(request);
        URI location = URI.create(httpRequest.getRequestURL().append('/').append(notification.getId()).toString());
        return ResponseEntity.created(location).body(notification);
    }

    @GetMapping("/{id}")
    public Notification get(@PathVariable String id) {
        return notificationService.get(id);
    }

    @GetMapping
    public List<Notification> findByRecipient(@RequestParam String recipient) {
        return notificationService.findByRecipient(recipient);
    }
}
