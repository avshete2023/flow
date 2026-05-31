package com.flow.connector.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.flow.connector.connector.ConnectorValidationModel;
import org.junit.jupiter.api.Test;

class LogConnectorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldValidateRequiredMessage() {
        LogConnector connector = new LogConnector(objectMapper);

        ConnectorValidationModel validation = connector.validate(objectMapper.createObjectNode());

        assertThat(validation.valid()).isFalse();
        assertThat(validation.errors()).contains("Log message is required");
    }

    @Test
    void shouldLogWithContextAndReturnStructuredOutput() {
        LogConnector connector = new LogConnector(objectMapper);

        ConnectorExecutionResult result = connector.execute(objectMapper.createObjectNode()
                .put("message", "Connector executed")
                .put("level", "INFO"), objectMapper.createObjectNode()
                .put("correlationId", "corr-123")
                .put("workflowId", "wf-1"));

        assertThat(result.success()).isTrue();
        assertThat(result.output().path("logged").asBoolean()).isTrue();
        assertThat(result.output().path("correlationId").asText()).isEqualTo("corr-123");
        assertThat(result.output().path("message").asText()).isEqualTo("Connector executed");
    }
}

