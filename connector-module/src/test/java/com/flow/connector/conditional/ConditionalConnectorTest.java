package com.flow.connector.conditional;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.connector.connector.ConnectorExecutionResult;
import com.flow.connector.connector.ConnectorValidationModel;
import org.junit.jupiter.api.Test;

class ConditionalConnectorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldEvaluateLiteralConditionAndRouteBranch() {
        ConditionalConnector connector = new ConditionalConnector(objectMapper);

        ConnectorExecutionResult result = connector.execute(objectMapper.createObjectNode()
                .put("expression", "true")
                .put("onTrue", "approve")
                .put("onFalse", "reject"), objectMapper.createObjectNode());

        assertThat(result.success()).isTrue();
        assertThat(result.output().path("conditionMet").asBoolean()).isTrue();
        assertThat(result.output().path("branch").asText()).isEqualTo("approve");
    }

    @Test
    void shouldEvaluateInputExpression() {
        ConditionalConnector connector = new ConditionalConnector(objectMapper);

        ConnectorExecutionResult result = connector.execute(objectMapper.createObjectNode()
                .put("expression", "input.flags.isPriority")
                .put("onTrue", "priorityPath")
                .put("onFalse", "normalPath"), objectMapper.createObjectNode()
                .set("flags", objectMapper.createObjectNode().put("isPriority", true)));

        assertThat(result.success()).isTrue();
        assertThat(result.output().path("branch").asText()).isEqualTo("priorityPath");
    }

    @Test
    void shouldFailOnUnsupportedExpression() {
        ConditionalConnector connector = new ConditionalConnector(objectMapper);

        ConnectorExecutionResult result = connector.execute(objectMapper.createObjectNode()
                .put("expression", "amount > 10"), objectMapper.createObjectNode());

        assertThat(result.success()).isFalse();
        assertThat(result.errorMessage()).contains("Unsupported condition expression");
    }

    @Test
    void shouldValidateRequiredExpression() {
        ConditionalConnector connector = new ConditionalConnector(objectMapper);

        ConnectorValidationModel validation = connector.validate(objectMapper.createObjectNode());

        assertThat(validation.valid()).isFalse();
        assertThat(validation.errors()).contains("Condition expression is required");
    }
}

