package com.flow.connector.trigger;

import java.util.UUID;

/**
 * Response payload for webhook acceptance.
 */
public record WebhookPayloadResponse(
        UUID workflowId,
        UUID executionRequestId,
        String status
) {
}

