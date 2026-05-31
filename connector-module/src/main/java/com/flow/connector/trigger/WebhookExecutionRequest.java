package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Execution request generated from a validated webhook call.
 */
public record WebhookExecutionRequest(
        UUID id,
        UUID workflowId,
        JsonNode payload,
        LocalDateTime createdAt
) {
}

