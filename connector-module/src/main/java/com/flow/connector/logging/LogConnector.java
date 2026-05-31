package com.flow.connector.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.connector.Connector;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.flow.connector.connector.ConnectorMetadata;
import com.flow.connector.connector.ConnectorType;
import com.flow.connector.connector.ConnectorValidationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Connector that writes structured logs and forwards correlation context.
 */
@Component
public class LogConnector implements Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogConnector.class);

    private final ObjectMapper objectMapper;

    public LogConnector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ConnectorValidationModel validate(JsonNode configuration) {
        return LogConnectorConfiguration.fromJson(configuration).validate();
    }

    @Override
    public ConnectorExecutionResult execute(JsonNode configuration, JsonNode input) {
        LogConnectorConfiguration parsed = LogConnectorConfiguration.fromJson(configuration);
        ConnectorValidationModel validation = parsed.validate();
        if (!validation.valid()) {
            return ConnectorExecutionResult.failure("Invalid log connector configuration: " + String.join("; ", validation.errors()));
        }

        String correlationId = input != null ? input.path("correlationId").asText("n/a") : "n/a";
        String context = input == null ? "{}" : input.toString();
        log(parsed.level(), correlationId, parsed.message(), context);

        ObjectNode output = objectMapper.createObjectNode()
                .put("logged", true)
                .put("level", parsed.level().name())
                .put("message", parsed.message())
                .put("correlationId", correlationId);

        return ConnectorExecutionResult.success(output);
    }

    @Override
    public ConnectorMetadata getMetadata() {
        return new ConnectorMetadata(
                ConnectorType.LOG,
                "Log Connector",
                "Writes structured logs with workflow context"
        );
    }

    private void log(LogLevel level, String correlationId, String message, String context) {
        switch (level) {
            case DEBUG -> LOGGER.debug("connector=LOG correlationId={} message={} context={}", correlationId, message, context);
            case WARN -> LOGGER.warn("connector=LOG correlationId={} message={} context={}", correlationId, message, context);
            case ERROR -> LOGGER.error("connector=LOG correlationId={} message={} context={}", correlationId, message, context);
            default -> LOGGER.info("connector=LOG correlationId={} message={} context={}", correlationId, message, context);
        }
    }
}

