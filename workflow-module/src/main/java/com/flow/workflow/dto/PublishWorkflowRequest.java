package com.flow.workflow.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for publishing a workflow definition.
 */
public record PublishWorkflowRequest(
        @NotBlank(message = "Workflow definition cannot be blank")
        String definitionJson
) {
}

