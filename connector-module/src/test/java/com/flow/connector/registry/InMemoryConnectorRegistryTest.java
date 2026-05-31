package com.flow.connector.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.connector.Connector;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.flow.connector.connector.ConnectorMetadata;
import com.flow.connector.connector.ConnectorType;
import com.flow.connector.connector.ConnectorValidationModel;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryConnectorRegistryTest {

    @Test
    void shouldLookupRegisteredConnector() {
        Connector connector = new SampleConnector(ConnectorType.HTTP);
        InMemoryConnectorRegistry registry = new InMemoryConnectorRegistry(List.of(connector));

        Connector resolved = registry.get(ConnectorType.HTTP);

        assertThat(resolved).isSameAs(connector);
        assertThat(registry.supports(ConnectorType.HTTP)).isTrue();
        assertThat(registry.supports(ConnectorType.DELAY)).isFalse();
    }

    @Test
    void shouldThrowForUnknownConnectorType() {
        InMemoryConnectorRegistry registry = new InMemoryConnectorRegistry(List.of(new SampleConnector(ConnectorType.LOG)));

        assertThatThrownBy(() -> registry.get(ConnectorType.HTTP))
                .isInstanceOf(InMemoryConnectorRegistry.UnknownConnectorTypeException.class)
                .hasMessage("Unknown connector type: HTTP");
    }

    @Test
    void shouldRejectDuplicateConnectorRegistration() {
        Connector first = new SampleConnector(ConnectorType.HTTP);
        Connector second = new AnotherSampleConnector(ConnectorType.HTTP);

        assertThatThrownBy(() -> new InMemoryConnectorRegistry(List.of(first, second)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Duplicate connector registration for type: HTTP");
    }

    private static class SampleConnector implements Connector {

        private final ConnectorType connectorType;

        private SampleConnector(ConnectorType connectorType) {
            this.connectorType = connectorType;
        }

        @Override
        public ConnectorValidationModel validate(JsonNode configuration) {
            return ConnectorValidationModel.success();
        }

        @Override
        public ConnectorExecutionResult execute(JsonNode configuration, JsonNode input) {
            ObjectNode output = JsonNodeFactory.instance.objectNode();
            output.put("status", "ok");
            return ConnectorExecutionResult.success(output);
        }

        @Override
        public ConnectorMetadata getMetadata() {
            return new ConnectorMetadata(connectorType, "Sample", "Sample connector");
        }
    }

    private static class AnotherSampleConnector extends SampleConnector {
        private AnotherSampleConnector(ConnectorType connectorType) {
            super(connectorType);
        }
    }
}

