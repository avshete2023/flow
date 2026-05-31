package com.flow.connector.delay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.connector.Connector;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.flow.connector.connector.ConnectorMetadata;
import com.flow.connector.connector.ConnectorType;
import com.flow.connector.connector.ConnectorValidationModel;
import org.springframework.stereotype.Component;

/**
 * Connector that introduces an execution delay.
 */
@Component
public class DelayConnector implements Connector {

    private final ObjectMapper objectMapper;

    public DelayConnector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ConnectorValidationModel validate(JsonNode configuration) {
        return DelayConnectorConfiguration.fromJson(configuration).validate();
    }

    @Override
    public ConnectorExecutionResult execute(JsonNode configuration, JsonNode input) {
        DelayConnectorConfiguration parsed = DelayConnectorConfiguration.fromJson(configuration);
        ConnectorValidationModel validation = parsed.validate();
        if (!validation.valid()) {
            return ConnectorExecutionResult.failure("Invalid delay connector configuration: " + String.join("; ", validation.errors()));
        }

        try {
            Thread.sleep(parsed.durationMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ConnectorExecutionResult.failure("Delay execution interrupted");
        }

        ObjectNode output = objectMapper.createObjectNode()
                .put("status", "DELAY_COMPLETED")
                .put("delayedMillis", parsed.durationMillis());

        if (input != null) {
            output.set("input", input);
        }

        return ConnectorExecutionResult.success(output);
    }

    @Override
    public ConnectorMetadata getMetadata() {
        return new ConnectorMetadata(
                ConnectorType.DELAY,
                "Delay Connector",
                "Pauses execution for the configured duration"
        );
    }
}

