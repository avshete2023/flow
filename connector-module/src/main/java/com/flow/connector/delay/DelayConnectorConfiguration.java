package com.flow.connector.delay;

import com.fasterxml.jackson.databind.JsonNode;
import com.flow.connector.connector.ConnectorValidationModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Delay connector configuration model.
 */
public record DelayConnectorConfiguration(Long durationMillis) {

    public static DelayConnectorConfiguration fromJson(JsonNode configuration) {
        if (configuration == null || !configuration.isObject()) {
            return new DelayConnectorConfiguration(null);
        }

        JsonNode durationNode = configuration.get("durationMillis");
        if (durationNode == null || !durationNode.canConvertToLong()) {
            return new DelayConnectorConfiguration(null);
        }

        return new DelayConnectorConfiguration(durationNode.asLong());
    }

    public ConnectorValidationModel validate() {
        List<String> errors = new ArrayList<>();
        if (durationMillis == null) {
            errors.add("Delay duration is required");
        } else if (durationMillis <= 0) {
            errors.add("Delay duration must be greater than zero");
        }

        return errors.isEmpty() ? ConnectorValidationModel.success() : ConnectorValidationModel.failure(errors);
    }
}

