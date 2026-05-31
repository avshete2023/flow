package com.flow.workflow.dto;

import com.flow.workflow.domain.model.WorkflowStatus;
import java.util.UUID;

/**
 * Response DTO for workflow data.
 */
public record WorkflowResponse(
        UUID workflowId,
        String name,
        String description,
        WorkflowStatus status,
        UUID activeVersionId
) {
}

