package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generic trigger definition model for all trigger types.
 */
public record TriggerDefinition(
        @NotNull(message = "Trigger id is required")
        UUID id,

        @NotNull(message = "Workflow version id is required")
        UUID workflowVersionId,

        @NotNull(message = "Trigger type is required")
        TriggerType triggerType,

        @NotBlank(message = "Trigger name is required")
        String name,

        @NotNull(message = "Trigger configuration is required")
        JsonNode configuration,

        boolean enabled
) {

    public TriggerValidationModel validate() {
        List<String> errors = new ArrayList<>();

        if (id == null) {
            errors.add("Trigger id is required");
        }
        if (workflowVersionId == null) {
            errors.add("Workflow version id is required");
        }
        if (triggerType == null) {
            errors.add("Trigger type is required");
        }
        if (name == null || name.isBlank()) {
            errors.add("Trigger name is required");
        }
        if (configuration == null || configuration.isNull() || !configuration.isObject() || configuration.isEmpty()) {
            errors.add("Trigger configuration must be a non-empty JSON object");
        }

        return errors.isEmpty() ? TriggerValidationModel.success() : TriggerValidationModel.failure(errors);
    }
}

