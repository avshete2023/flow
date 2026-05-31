package com.flow.workflow.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when a workflow is published.
 */
public record WorkflowPublishedAuditEvent(
        UUID workflowId,
        UUID workflowVersionId,
        int version,
        UUID actorId,
        LocalDateTime publishedAt
) {
}

