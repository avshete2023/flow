package com.flow.connector.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class WebhookTriggerHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebhookTriggerHandler webhookTriggerHandler = new WebhookTriggerHandler();

    @Test
    void shouldValidateWebhookConfigurationSuccessfully() {
        TriggerValidationModel validationModel = webhookTriggerHandler.validateConfiguration(objectMapper.createObjectNode()
                .put("webhookIdentifier", "wh_1234567890abcdef1234567890abcdef")
                .put("secretToken", "super-secret-token-1234")
                .put("active", true));

        assertThat(webhookTriggerHandler.getSupportedType()).isEqualTo(TriggerType.WEBHOOK);
        assertThat(validationModel.valid()).isTrue();
    }

    @Test
    void shouldRejectMissingSecretToken() {
        TriggerValidationModel validationModel = webhookTriggerHandler.validateConfiguration(objectMapper.createObjectNode()
                .put("webhookIdentifier", "wh_1234567890abcdef1234567890abcdef")
                .put("active", true));

        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Secret token is required");
    }

    @Test
    void shouldRejectInvalidWebhookIdentifierFormat() {
        TriggerValidationModel validationModel = webhookTriggerHandler.validateConfiguration(objectMapper.createObjectNode()
                .put("webhookIdentifier", "webhook-1")
                .put("secretToken", "super-secret-token-1234")
                .put("active", true));

        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Webhook identifier format is invalid");
    }

    @Test
    void shouldRejectInvalidSecretLengthAndNonBooleanActive() {
        TriggerValidationModel validationModel = webhookTriggerHandler.validateConfiguration(objectMapper.createObjectNode()
                .put("webhookIdentifier", "wh_1234567890abcdef1234567890abcdef")
                .put("secretToken", "short")
                .put("active", "yes"));

        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Secret token must be between 16 and 128 characters");
        assertThat(validationModel.errors()).contains("Active flag must be boolean");
    }
}

