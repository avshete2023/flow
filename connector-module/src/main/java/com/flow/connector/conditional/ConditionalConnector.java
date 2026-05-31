package com.flow.connector.conditional;

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
 * Connector that evaluates boolean conditions and chooses a branch.
 */
@Component
public class ConditionalConnector implements Connector {

    private final ObjectMapper objectMapper;

    public ConditionalConnector(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ConnectorValidationModel validate(JsonNode configuration) {
        return ConditionalConnectorConfiguration.fromJson(configuration).validate();
    }

    @Override
    public ConnectorExecutionResult execute(JsonNode configuration, JsonNode input) {
        ConditionalConnectorConfiguration parsed = ConditionalConnectorConfiguration.fromJson(configuration);
        ConnectorValidationModel validation = parsed.validate();
        if (!validation.valid()) {
            return ConnectorExecutionResult.failure("Invalid conditional connector configuration: " + String.join("; ", validation.errors()));
        }

        EvaluationResult evaluationResult = evaluate(parsed.expression(), input);
        if (!evaluationResult.supported()) {
            return ConnectorExecutionResult.failure("Unsupported condition expression: " + parsed.expression());
        }

        boolean conditionMet = evaluationResult.result();
        String branch = conditionMet ? parsed.onTrue() : parsed.onFalse();

        ObjectNode output = objectMapper.createObjectNode()
                .put("conditionMet", conditionMet)
                .put("branch", branch)
                .put("expression", parsed.expression());

        return ConnectorExecutionResult.success(output);
    }

    @Override
    public ConnectorMetadata getMetadata() {
        return new ConnectorMetadata(
                ConnectorType.CONDITIONAL,
                "Conditional Connector",
                "Evaluates a condition and routes execution to true/false branch"
        );
    }

    private EvaluationResult evaluate(String expression, JsonNode input) {
        if ("true".equalsIgnoreCase(expression)) {
            return new EvaluationResult(true, true);
        }
        if ("false".equalsIgnoreCase(expression)) {
            return new EvaluationResult(true, false);
        }
        if (expression != null && expression.startsWith("input.")) {
            JsonNode valueNode = resolveInputPath(input, expression.substring("input.".length()));
            if (valueNode == null || valueNode.isMissingNode() || valueNode.isNull()) {
                return new EvaluationResult(false, false);
            }
            if (valueNode.isBoolean()) {
                return new EvaluationResult(true, valueNode.asBoolean());
            }
            if (valueNode.isTextual()) {
                String text = valueNode.asText();
                if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
                    return new EvaluationResult(true, Boolean.parseBoolean(text));
                }
            }
            return new EvaluationResult(false, false);
        }

        return new EvaluationResult(false, false);
    }

    private JsonNode resolveInputPath(JsonNode input, String path) {
        if (input == null || !input.isObject()) {
            return null;
        }
        String[] parts = path.split("\\.");
        JsonNode current = input;
        for (String part : parts) {
            if (part.isBlank()) {
                return null;
            }
            current = current.path(part);
        }
        return current;
    }

    private record EvaluationResult(boolean supported, boolean result) {
    }
}

