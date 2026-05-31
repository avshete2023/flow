package com.flow.connector.conditional;

import com.fasterxml.jackson.databind.JsonNode;
import com.flow.connector.connector.ConnectorValidationModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Conditional connector configuration model.
 */
public record ConditionalConnectorConfiguration(
        String expression,
        String onTrue,
        String onFalse
) {

    public static ConditionalConnectorConfiguration fromJson(JsonNode configuration) {
        if (configuration == null || !configuration.isObject()) {
            return new ConditionalConnectorConfiguration(null, "true", "false");
        }

        String expression = text(configuration.get("expression"));
        String onTrue = defaultIfBlank(text(configuration.get("onTrue")), "true");
        String onFalse = defaultIfBlank(text(configuration.get("onFalse")), "false");

        return new ConditionalConnectorConfiguration(expression, onTrue, onFalse);
    }

    public ConnectorValidationModel validate() {
        List<String> errors = new ArrayList<>();
        if (expression == null) {
            errors.add("Condition expression is required");
        }

        return errors.isEmpty() ? ConnectorValidationModel.success() : ConnectorValidationModel.failure(errors);
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

