package com.flow.connector.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.flow.connector.registry.ConnectorRegistry;
import com.flow.connector.registry.InMemoryConnectorRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Service for connector-specific validation and validation result aggregation.
 */
@Service
public class ConnectorValidationService {

    private final ConnectorRegistry connectorRegistry;

    public ConnectorValidationService(ConnectorRegistry connectorRegistry) {
        this.connectorRegistry = connectorRegistry;
    }

    public ConnectorValidationModel validate(ConnectorType connectorType, JsonNode configuration) {
        if (connectorType == null) {
            return ConnectorValidationModel.failure(List.of("Connector type is required"));
        }

        try {
            Connector connector = connectorRegistry.get(connectorType);
            return connector.validate(configuration);
        } catch (InMemoryConnectorRegistry.UnknownConnectorTypeException exception) {
            return ConnectorValidationModel.failure(List.of(exception.getMessage()));
        }
    }

    public ConnectorValidationSummary validateAll(Map<ConnectorType, JsonNode> connectorConfigurations) {
        if (connectorConfigurations == null || connectorConfigurations.isEmpty()) {
            return ConnectorValidationSummary.of(Map.of(), List.of("Connector configuration map is required"));
        }

        Map<ConnectorType, ConnectorValidationModel> results = new LinkedHashMap<>();
        List<String> aggregatedErrors = new ArrayList<>();

        for (Map.Entry<ConnectorType, JsonNode> entry : connectorConfigurations.entrySet()) {
            ConnectorType connectorType = entry.getKey();
            ConnectorValidationModel result = validate(connectorType, entry.getValue());
            results.put(connectorType, result);

            if (!result.valid()) {
                for (String error : result.errors()) {
                    aggregatedErrors.add((connectorType == null ? "UNKNOWN" : connectorType.name()) + ": " + error);
                }
            }
        }

        return ConnectorValidationSummary.of(results, aggregatedErrors);
    }
}

