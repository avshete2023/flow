package com.flow.workflow.domain.model;

import java.util.List;

/**
 * Result of validating a workflow definition.
 */
public record WorkflowValidationResult(
        boolean valid,
        List<String> errors
) {

    public static WorkflowValidationResult success() {
        return new WorkflowValidationResult(true, List.of());
    }

    public static WorkflowValidationResult failure(List<String> errors) {
        return new WorkflowValidationResult(false, List.copyOf(errors));
    }
}


