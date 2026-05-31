package com.flow.connector.connector;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

class ConnectorContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSupportValidateExecuteAndMetadataContract() {
        Connector connector = new SampleConnector();
        JsonNode configuration = objectMapper.createObjectNode().put("key", "value");
        JsonNode input = objectMapper.createObjectNode().put("payload", "example");

        ConnectorValidationModel validation = connector.validate(configuration);
        ConnectorExecutionResult execution = connector.execute(configuration, input);
        ConnectorMetadata metadata = connector.getMetadata();

        assertThat(validation.valid()).isTrue();
        assertThat(execution.success()).isTrue();
        assertThat(execution.output().path("payload").asText()).isEqualTo("example");
        assertThat(metadata.connectorType()).isEqualTo(ConnectorType.HTTP);
        assertThat(metadata.name()).isEqualTo("Sample HTTP Connector");
    }

    private static final class SampleConnector implements Connector {

        @Override
        public ConnectorValidationModel validate(JsonNode configuration) {
            if (configuration == null || !configuration.isObject()) {
                return ConnectorValidationModel.failure(java.util.List.of("Configuration must be an object"));
            }
            return ConnectorValidationModel.success();
        }

        @Override
        public ConnectorExecutionResult execute(JsonNode configuration, JsonNode input) {
            ObjectNode output = JsonNodeFactory.instance.objectNode();
            output.set("payload", input.path("payload"));
            return ConnectorExecutionResult.success(output);
        }

        @Override
        public ConnectorMetadata getMetadata() {
            return new ConnectorMetadata(ConnectorType.HTTP, "Sample HTTP Connector", "Contract test connector");
        }
    }
}

