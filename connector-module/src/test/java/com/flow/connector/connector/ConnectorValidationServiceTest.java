package com.flow.connector.connector;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.registry.InMemoryConnectorRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConnectorValidationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ConnectorValidationService connectorValidationService;

    @BeforeEach
    void setUp() {
        connectorValidationService = new ConnectorValidationService(new InMemoryConnectorRegistry(List.of(
                new TestConnector(ConnectorType.HTTP, true),
                new TestConnector(ConnectorType.DELAY, false)
        )));
    }

    @Test
    void shouldValidateSpecificConnectorConfiguration() {
        ObjectNode configuration = objectMapper.createObjectNode().put("url", "https://example.com");

        ConnectorValidationModel result = connectorValidationService.validate(ConnectorType.HTTP, configuration);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldAggregateValidationResultsAcrossConnectors() {
        Map<ConnectorType, com.fasterxml.jackson.databind.JsonNode> configurations = new LinkedHashMap<>();
        configurations.put(ConnectorType.HTTP, objectMapper.createObjectNode().put("url", "https://example.com"));
        configurations.put(ConnectorType.DELAY, objectMapper.createObjectNode().put("durationMillis", 1000));

        ConnectorValidationSummary result = connectorValidationService.validateAll(configurations);

        assertThat(result.valid()).isFalse();
        assertThat(result.results()).hasSize(2);
        assertThat(result.results().get(ConnectorType.HTTP).valid()).isTrue();
        assertThat(result.results().get(ConnectorType.DELAY).valid()).isFalse();
        assertThat(result.errors()).contains("DELAY: Invalid DELAY configuration");
    }

    @Test
    void shouldReturnFailureForUnknownConnectorType() {
        ConnectorValidationModel result = connectorValidationService.validate(ConnectorType.LOG, objectMapper.createObjectNode());

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Unknown connector type: LOG");
    }

    private static class TestConnector implements Connector {

        private final ConnectorType connectorType;
        private final boolean valid;

        private TestConnector(ConnectorType connectorType, boolean valid) {
            this.connectorType = connectorType;
            this.valid = valid;
        }

        @Override
        public ConnectorValidationModel validate(com.fasterxml.jackson.databind.JsonNode configuration) {
            if (valid) {
                return ConnectorValidationModel.success();
            }
            return ConnectorValidationModel.failure(List.of("Invalid " + connectorType + " configuration"));
        }

        @Override
        public ConnectorExecutionResult execute(
                com.fasterxml.jackson.databind.JsonNode configuration,
                com.fasterxml.jackson.databind.JsonNode input
        ) {
            throw new UnsupportedOperationException("Not needed for validation tests");
        }

        @Override
        public ConnectorMetadata getMetadata() {
            return new ConnectorMetadata(connectorType, connectorType.name(), "Test connector");
        }
    }
}

