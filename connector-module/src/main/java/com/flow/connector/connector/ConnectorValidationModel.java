package com.flow.connector.connector;

import java.util.List;

/**
 * Result contract for connector validation.
 */
public record ConnectorValidationModel(
        boolean valid,
        List<String> errors
) {

    public static ConnectorValidationModel success() {
        return new ConnectorValidationModel(true, List.of());
    }

    public static ConnectorValidationModel failure(List<String> errors) {
        return new ConnectorValidationModel(false, List.copyOf(errors));
    }
}

