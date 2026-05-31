package com.flow.connector.registry;

import com.flow.connector.connector.Connector;
import com.flow.connector.connector.ConnectorType;

/**
 * Registry for connector lookup by connector type.
 */
public interface ConnectorRegistry {

    Connector get(ConnectorType connectorType);

    boolean supports(ConnectorType connectorType);
}

