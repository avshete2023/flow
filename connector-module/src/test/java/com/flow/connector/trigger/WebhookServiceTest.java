package com.flow.connector.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WebhookServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private InMemoryWebhookExecutionStore webhookExecutionStore;
    private InMemoryWebhookExecutionPublisher webhookExecutionPublisher;
    private WebhookService webhookService;

    private UUID workflowId;

    @BeforeEach
    void setUp() {
        webhookExecutionStore = new InMemoryWebhookExecutionStore();
        webhookExecutionPublisher = new InMemoryWebhookExecutionPublisher();
        webhookService = new WebhookService(webhookExecutionStore, webhookExecutionPublisher);
        workflowId = UUID.randomUUID();

        WebhookTriggerDefinition triggerDefinition = new WebhookTriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Webhook Trigger",
                "wh_1234567890abcdef1234567890abcdef",
                "super-secret-token-1234",
                true
        );
        webhookExecutionStore.register(workflowId, triggerDefinition);
    }

    @Test
    void shouldCreateExecutionRequestWhenSecretIsValid() {
        WebhookPayloadRequest request = new WebhookPayloadRequest(
                "super-secret-token-1234",
                objectMapper.createObjectNode().put("event", "order.created")
        );

        WebhookPayloadResponse response = webhookService.handleWebhook(workflowId, request);

        assertThat(response.workflowId()).isEqualTo(workflowId);
        assertThat(response.executionRequestId()).isNotNull();
        assertThat(response.status()).isEqualTo("QUEUED");
        assertThat(webhookExecutionPublisher.publishedRequests()).hasSize(1);
        assertThat(webhookExecutionStore.getPayload(response.executionRequestId())).isPresent();
    }

    @Test
    void shouldRejectWebhookWhenSecretIsInvalid() {
        WebhookPayloadRequest request = new WebhookPayloadRequest(
                "wrong-secret-token",
                objectMapper.createObjectNode().put("event", "order.created")
        );

        assertThatThrownBy(() -> webhookService.handleWebhook(workflowId, request))
                .isInstanceOf(WebhookService.InvalidWebhookSecretException.class)
                .hasMessageContaining("Invalid webhook secret");
    }
}

