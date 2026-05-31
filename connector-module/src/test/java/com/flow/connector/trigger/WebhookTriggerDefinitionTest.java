package com.flow.connector.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WebhookTriggerDefinitionTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldGenerateUniqueWebhookIdentifierAndSupportActiveStatus() {
        WebhookTriggerDefinition first = WebhookTriggerDefinition.createNew(
                UUID.randomUUID(),
                "Webhook Trigger",
                "super-secret-token-0001",
                true
        );
        WebhookTriggerDefinition second = WebhookTriggerDefinition.createNew(
                UUID.randomUUID(),
                "Webhook Trigger",
                "super-secret-token-0002",
                false
        );

        assertThat(first.webhookIdentifier()).startsWith("wh_");
        assertThat(second.webhookIdentifier()).startsWith("wh_");
        assertThat(first.webhookIdentifier()).isNotEqualTo(second.webhookIdentifier());
        assertThat(first.active()).isTrue();
        assertThat(second.active()).isFalse();
    }

    @Test
    void shouldSupportSecretTokenAndConvertToGenericTriggerDefinition() {
        WebhookTriggerDefinition webhook = WebhookTriggerDefinition.createNew(
                UUID.randomUUID(),
                "Webhook Trigger",
                "super-secret-token-12345",
                true
        );

        TriggerDefinition triggerDefinition = webhook.toTriggerDefinition();

        assertThat(triggerDefinition.triggerType()).isEqualTo(TriggerType.WEBHOOK);
        assertThat(triggerDefinition.configuration().get("webhookIdentifier").asText()).isEqualTo(webhook.webhookIdentifier());
        assertThat(triggerDefinition.configuration().get("secretToken").asText()).isEqualTo("super-secret-token-12345");
        assertThat(triggerDefinition.configuration().get("active").asBoolean()).isTrue();
    }

    @Test
    void shouldPassBeanAndDomainValidationForValidWebhookTrigger() {
        WebhookTriggerDefinition webhook = WebhookTriggerDefinition.createNew(
                UUID.randomUUID(),
                "Webhook Trigger",
                "super-secret-token-valid",
                true
        );

        Set<?> violations = validator.validate(webhook);
        TriggerValidationModel validationModel = webhook.validate();

        assertThat(violations).isEmpty();
        assertThat(validationModel.valid()).isTrue();
        assertThat(validationModel.errors()).isEmpty();
    }

    @Test
    void shouldRejectInvalidWebhookSecretTokenAndMissingIdentifier() {
        WebhookTriggerDefinition invalid = new WebhookTriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Webhook Trigger",
                "",
                "short",
                true
        );

        TriggerValidationModel validationModel = invalid.validate();

        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Webhook identifier is required");
        assertThat(validationModel.errors()).contains("Secret token must be between 16 and 128 characters");
    }
}

