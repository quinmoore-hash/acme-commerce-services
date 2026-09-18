package com.acme.notifications.web;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class NotificationRequest {

    @NotBlank
    @Email
    private String recipient;

    @NotBlank
    @Pattern(regexp = "EMAIL|SMS", message = "must be EMAIL or SMS")
    private String channel = "EMAIL";

    @NotBlank
    private String template;

    private Map<String, String> variables = new HashMap<>();

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
}
