package com.flow.execution.queue;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message payload published to execution queue.
 */
public record ExecutionRequestMessage(
        UUID executionId,
        UUID workflowVersionId,
        String correlationId,
        LocalDateTime requestedAt
) {
}

