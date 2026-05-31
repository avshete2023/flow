package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contract implemented by each trigger type.
 */
public interface TriggerHandler {

    TriggerType getSupportedType();

    TriggerValidationModel validateConfiguration(JsonNode configuration);
}

