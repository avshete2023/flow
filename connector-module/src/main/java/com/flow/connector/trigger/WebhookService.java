package com.flow.connector.trigger;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Service for validating webhook requests and creating execution commands.
 */
@Service
public class WebhookService {

    private final InMemoryWebhookExecutionStore webhookExecutionStore;
    private final WebhookExecutionPublisher webhookExecutionPublisher;

    public WebhookService(
            InMemoryWebhookExecutionStore webhookExecutionStore,
            WebhookExecutionPublisher webhookExecutionPublisher
    ) {
        this.webhookExecutionStore = webhookExecutionStore;
        this.webhookExecutionPublisher = webhookExecutionPublisher;
    }

    public WebhookPayloadResponse handleWebhook(UUID workflowId, WebhookPayloadRequest request) {
        WebhookTriggerDefinition triggerDefinition = webhookExecutionStore.getByWorkflowId(workflowId)
                .orElseThrow(() -> new WebhookConfigurationNotFoundException(workflowId));

        if (!triggerDefinition.active()) {
            throw new WebhookInactiveException(workflowId);
        }

        if (!triggerDefinition.secretToken().equals(request.secretToken())) {
            throw new InvalidWebhookSecretException();
        }

        UUID executionRequestId = UUID.randomUUID();
        webhookExecutionStore.storePayload(executionRequestId, request.payload());

        WebhookExecutionRequest executionRequest = new WebhookExecutionRequest(
                executionRequestId,
                workflowId,
                request.payload(),
                LocalDateTime.now()
        );
        webhookExecutionPublisher.publish(executionRequest);

        return new WebhookPayloadResponse(workflowId, executionRequestId, "QUEUED");
    }

    public static class WebhookConfigurationNotFoundException extends RuntimeException {
        public WebhookConfigurationNotFoundException(UUID workflowId) {
            super("Webhook trigger configuration not found for workflow: " + workflowId);
        }
    }

    public static class InvalidWebhookSecretException extends RuntimeException {
        public InvalidWebhookSecretException() {
            super("Invalid webhook secret token");
        }
    }

    public static class WebhookInactiveException extends RuntimeException {
        public WebhookInactiveException(UUID workflowId) {
            super("Webhook trigger is inactive for workflow: " + workflowId);
        }
    }
}


