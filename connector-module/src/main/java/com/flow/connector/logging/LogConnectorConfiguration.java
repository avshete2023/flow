package com.flow.connector.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.flow.connector.connector.ConnectorValidationModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Log connector configuration model.
 */
public record LogConnectorConfiguration(
        String message,
        LogLevel level
) {

    public static LogConnectorConfiguration fromJson(JsonNode configuration) {
        if (configuration == null || !configuration.isObject()) {
            return new LogConnectorConfiguration(null, LogLevel.INFO);
        }

        String message = text(configuration.get("message"));
        LogLevel level = parseLevel(configuration.get("level"));

        return new LogConnectorConfiguration(message, level == null ? LogLevel.INFO : level);
    }

    public ConnectorValidationModel validate() {
        List<String> errors = new ArrayList<>();
        if (message == null) {
            errors.add("Log message is required");
        }

        return errors.isEmpty() ? ConnectorValidationModel.success() : ConnectorValidationModel.failure(errors);
    }

    private static LogLevel parseLevel(JsonNode levelNode) {
        String value = text(levelNode);
        if (value == null) {
            return null;
        }

        try {
            return LogLevel.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }
}

