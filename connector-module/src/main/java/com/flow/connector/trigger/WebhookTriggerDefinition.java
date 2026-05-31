package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Webhook trigger-specific configuration model.
 */
public record WebhookTriggerDefinition(
        @NotNull(message = "Trigger id is required")
        UUID id,

        @NotNull(message = "Workflow version id is required")
        UUID workflowVersionId,

        @NotBlank(message = "Trigger name is required")
        String name,

        @NotBlank(message = "Webhook identifier is required")
        String webhookIdentifier,

        @NotBlank(message = "Secret token is required")
        @Size(min = 16, max = 128, message = "Secret token must be between 16 and 128 characters")
        String secretToken,

        boolean active
) {

    public static WebhookTriggerDefinition createNew(
            UUID workflowVersionId,
            String name,
            String secretToken,
            boolean active
    ) {
        return new WebhookTriggerDefinition(
                UUID.randomUUID(),
                workflowVersionId,
                name,
                generateWebhookIdentifier(),
                secretToken,
                active
        );
    }

    public TriggerValidationModel validate() {
        List<String> errors = new ArrayList<>();

        if (id == null) {
            errors.add("Trigger id is required");
        }
        if (workflowVersionId == null) {
            errors.add("Workflow version id is required");
        }
        if (name == null || name.isBlank()) {
            errors.add("Trigger name is required");
        }
        if (webhookIdentifier == null || webhookIdentifier.isBlank()) {
            errors.add("Webhook identifier is required");
        }
        if (secretToken == null || secretToken.isBlank()) {
            errors.add("Secret token is required");
        } else if (secretToken.length() < 16 || secretToken.length() > 128) {
            errors.add("Secret token must be between 16 and 128 characters");
        }

        return errors.isEmpty() ? TriggerValidationModel.success() : TriggerValidationModel.failure(errors);
    }

    public TriggerDefinition toTriggerDefinition() {
        ObjectNode configuration = JsonNodeFactory.instance.objectNode()
                .put("webhookIdentifier", webhookIdentifier)
                .put("secretToken", secretToken)
                .put("active", active);

        return new TriggerDefinition(
                id,
                workflowVersionId,
                TriggerType.WEBHOOK,
                name,
                configuration,
                active
        );
    }

    public static WebhookTriggerDefinition fromTriggerDefinition(TriggerDefinition triggerDefinition) {
        ObjectNode configuration = (ObjectNode) triggerDefinition.configuration();
        return new WebhookTriggerDefinition(
                triggerDefinition.id(),
                triggerDefinition.workflowVersionId(),
                triggerDefinition.name(),
                configuration.path("webhookIdentifier").asText(),
                configuration.path("secretToken").asText(),
                configuration.path("active").asBoolean(triggerDefinition.enabled())
        );
    }

    private static String generateWebhookIdentifier() {
        return "wh_" + UUID.randomUUID().toString().replace("-", "");
    }
}

