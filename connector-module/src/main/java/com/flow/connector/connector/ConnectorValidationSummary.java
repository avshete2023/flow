package com.flow.connector.connector;

import java.util.List;
import java.util.Map;

/**
 * Aggregated connector validation result across one or more connector configs.
 */
public record ConnectorValidationSummary(
        boolean valid,
        Map<ConnectorType, ConnectorValidationModel> results,
        List<String> errors
) {

    public static ConnectorValidationSummary of(
            Map<ConnectorType, ConnectorValidationModel> results,
            List<String> errors
    ) {
        return new ConnectorValidationSummary(errors.isEmpty(), Map.copyOf(results), List.copyOf(errors));
    }
}

