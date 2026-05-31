package com.flow.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a workflow.
 */
public record CreateWorkflowRequest(
        @NotBlank(message = "Workflow name cannot be blank")
        @Size(max = 255, message = "Workflow name must be at most 255 characters")
        String name,

        @Size(max = 5000, message = "Workflow description must be at most 5000 characters")
        String description
) {
}

