package com.flow.workflow.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when workflow lifecycle events occur (create, update, publish, activate, etc.).
 */
public record WorkflowLifecycleAuditEvent(
        UUID workflowId,
        UUID userId,
        String eventType,
        String details,
        LocalDateTime occurredAt
) {
}

