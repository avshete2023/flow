package com.flow.connector.connector;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Standard connector execution response.
 */
public record ConnectorExecutionResult(
        boolean success,
        JsonNode output,
        String errorMessage
) {

    public static ConnectorExecutionResult success(JsonNode output) {
        return new ConnectorExecutionResult(true, output, null);
    }

    public static ConnectorExecutionResult failure(String errorMessage) {
        return new ConnectorExecutionResult(false, null, errorMessage);
    }
}

