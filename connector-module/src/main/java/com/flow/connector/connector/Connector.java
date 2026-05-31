package com.flow.connector.connector;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contract implemented by all connectors.
 */
public interface Connector {

    ConnectorValidationModel validate(JsonNode configuration);

    ConnectorExecutionResult execute(JsonNode configuration, JsonNode input);

    ConnectorMetadata getMetadata();
}

