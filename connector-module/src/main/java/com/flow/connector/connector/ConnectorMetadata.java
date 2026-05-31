package com.flow.connector.connector;

/**
 * Descriptor used by registry and execution engine for connector lookup and discovery.
 */
public record ConnectorMetadata(
        ConnectorType connectorType,
        String name,
        String description
) {
}

