package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * In-memory storage for webhook trigger definitions and last payload snapshots.
 */
@Component
public class InMemoryWebhookExecutionStore {

    private final Map<UUID, WebhookTriggerDefinition> webhookByWorkflowId = new ConcurrentHashMap<>();
    private final Map<UUID, JsonNode> payloadByExecutionRequestId = new ConcurrentHashMap<>();

    public void register(UUID workflowId, WebhookTriggerDefinition webhookTriggerDefinition) {
        webhookByWorkflowId.put(workflowId, webhookTriggerDefinition);
    }

    public Optional<WebhookTriggerDefinition> getByWorkflowId(UUID workflowId) {
        return Optional.ofNullable(webhookByWorkflowId.get(workflowId));
    }

    public void storePayload(UUID executionRequestId, JsonNode payload) {
        payloadByExecutionRequestId.put(executionRequestId, payload);
    }

    public Optional<JsonNode> getPayload(UUID executionRequestId) {
        return Optional.ofNullable(payloadByExecutionRequestId.get(executionRequestId));
    }
}

