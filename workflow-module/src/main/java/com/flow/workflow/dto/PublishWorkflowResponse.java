package com.flow.workflow.dto;

import com.flow.workflow.domain.model.WorkflowStatus;
import java.util.UUID;

/**
 * Response DTO for workflow publish operation.
 */
public record PublishWorkflowResponse(
        UUID workflowVersionId,
        int version,
        WorkflowStatus status
) {
}

