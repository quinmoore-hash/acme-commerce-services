package com.acme.orders.config;

import java.time.Duration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConstructorBinding
@ConfigurationProperties(prefix = "acme.downstream")
public class DownstreamProperties {

    @NotBlank
    private final String inventoryBaseUrl;

    @NotBlank
    private final String notificationBaseUrl;

    @NotNull
    private final Duration connectTimeout;

    @NotNull
    private final Duration readTimeout;

    public DownstreamProperties(String inventoryBaseUrl, String notificationBaseUrl,
            @DefaultValue("2s") Duration connectTimeout, @DefaultValue("5s") Duration readTimeout) {
        this.inventoryBaseUrl = inventoryBaseUrl;
        this.notificationBaseUrl = notificationBaseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public String getInventoryBaseUrl() {
        return inventoryBaseUrl;
    }

    public String getNotificationBaseUrl() {
        return notificationBaseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }
}
