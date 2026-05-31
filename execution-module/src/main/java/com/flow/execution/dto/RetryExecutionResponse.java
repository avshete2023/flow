package com.flow.execution.dto;

import com.flow.execution.domain.model.ExecutionStatus;
import java.util.UUID;

/**
 * Response payload for manual execution retry endpoint.
 */
public record RetryExecutionResponse(
        UUID executionId,
        UUID retriedFromExecutionId,
        ExecutionStatus status
) {
}

