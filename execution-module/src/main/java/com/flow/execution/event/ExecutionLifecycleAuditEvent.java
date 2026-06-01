package com.flow.execution.event;

import com.flow.execution.domain.model.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when execution lifecycle events occur (start, completion, failure, DLQ movement).
 */
public record ExecutionLifecycleAuditEvent(
        UUID executionId,
        UUID workflowVersionId,
        String correlationId,
        String eventType,
        ExecutionStatus status,
        String details,
        LocalDateTime occurredAt
) {
}

