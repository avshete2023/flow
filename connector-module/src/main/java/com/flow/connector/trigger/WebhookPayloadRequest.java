package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for webhook execution endpoint.
 */
public record WebhookPayloadRequest(
        @NotBlank(message = "Secret token is required")
        String secretToken,

        @NotNull(message = "Payload is required")
        JsonNode payload
) {
}

