package com.flow.connector.registry;

import com.flow.connector.connector.Connector;
import com.flow.connector.connector.ConnectorType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * In-memory connector registry populated from Spring-managed connectors.
 */
@Component
public class InMemoryConnectorRegistry implements ConnectorRegistry {

    private final Map<ConnectorType, Connector> connectorsByType;

    public InMemoryConnectorRegistry(List<Connector> connectors) {
        this.connectorsByType = new EnumMap<>(ConnectorType.class);
        for (Connector connector : connectors) {
            ConnectorType connectorType = connector.getMetadata().connectorType();
            Connector existing = connectorsByType.putIfAbsent(connectorType, connector);
            if (existing != null) {
                throw new IllegalStateException("Duplicate connector registration for type: " + connectorType);
            }
        }
    }

    @Override
    public Connector get(ConnectorType connectorType) {
        Connector connector = connectorsByType.get(connectorType);
        if (connector == null) {
            throw new UnknownConnectorTypeException(connectorType);
        }
        return connector;
    }

    @Override
    public boolean supports(ConnectorType connectorType) {
        return connectorsByType.containsKey(connectorType);
    }

    public static class UnknownConnectorTypeException extends RuntimeException {
        public UnknownConnectorTypeException(ConnectorType connectorType) {
            super("Unknown connector type: " + connectorType);
        }
    }
}

