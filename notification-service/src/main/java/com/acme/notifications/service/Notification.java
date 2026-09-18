package com.acme.notifications.service;

import java.time.Instant;
import java.util.UUID;

public class Notification {

    private final String id = UUID.randomUUID().toString();
    private final String recipient;
    private final String channel;
    private final String template;
    private final String body;
    private final Instant createdAt = Instant.now();

    public Notification(String recipient, String channel, String template, String body) {
        this.recipient = recipient;
        this.channel = channel;
        this.template = template;
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getChannel() {
        return channel;
    }

    public String getTemplate() {
        return template;
    }

    public String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
