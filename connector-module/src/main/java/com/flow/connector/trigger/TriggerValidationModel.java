package com.flow.connector.trigger;

import java.util.List;

/**
 * Result contract for trigger validation.
 */
public record TriggerValidationModel(
        boolean valid,
        List<String> errors
) {

    public static TriggerValidationModel success() {
        return new TriggerValidationModel(true, List.of());
    }

    public static TriggerValidationModel failure(List<String> errors) {
        return new TriggerValidationModel(false, List.copyOf(errors));
    }
}

