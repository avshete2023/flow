package com.flow.execution.event;

import com.flow.execution.domain.model.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when execution status changes in the orchestrator.
 */
public record ExecutionAuditEvent(
        UUID executionId,
        UUID workflowVersionId,
        String correlationId,
        ExecutionStatus status,
        String errorMessage,
        LocalDateTime occurredAt
) {
}

