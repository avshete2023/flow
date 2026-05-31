package com.flow.connector.delay;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.flow.connector.connector.ConnectorValidationModel;
import org.junit.jupiter.api.Test;

class DelayConnectorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldValidateDelayConfiguration() {
        DelayConnector connector = new DelayConnector(objectMapper);

        ConnectorValidationModel validation = connector.validate(objectMapper.createObjectNode().put("durationMillis", 100));

        assertThat(validation.valid()).isTrue();
    }

    @Test
    void shouldRejectInvalidDelayConfiguration() {
        DelayConnector connector = new DelayConnector(objectMapper);

        ConnectorValidationModel validation = connector.validate(objectMapper.createObjectNode().put("durationMillis", 0));

        assertThat(validation.valid()).isFalse();
        assertThat(validation.errors()).contains("Delay duration must be greater than zero");
    }

    @Test
    void shouldDelayExecution() {
        DelayConnector connector = new DelayConnector(objectMapper);

        long start = System.currentTimeMillis();
        ConnectorExecutionResult result = connector.execute(objectMapper.createObjectNode().put("durationMillis", 40), null);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result.success()).isTrue();
        assertThat(result.output().path("status").asText()).isEqualTo("DELAY_COMPLETED");
        assertThat(elapsed).isGreaterThanOrEqualTo(25L);
    }
}

