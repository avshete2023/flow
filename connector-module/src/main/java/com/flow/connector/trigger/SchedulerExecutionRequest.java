package com.flow.connector.trigger;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Execution message emitted when a scheduler trigger fires.
 */
public record SchedulerExecutionRequest(
        UUID executionRequestId,
        UUID workflowId,
        UUID workflowVersionId,
        String correlationId,
        LocalDateTime requestedAt
) {
}

