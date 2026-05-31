package com.flow.connector.trigger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WebhookController.class)
@Import({WebhookService.class, InMemoryWebhookExecutionStore.class, InMemoryWebhookExecutionPublisher.class})
class WebhookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryWebhookExecutionStore webhookExecutionStore;

    private UUID workflowId;

    @BeforeEach
    void setUp() {
        workflowId = UUID.randomUUID();
        WebhookTriggerDefinition triggerDefinition = new WebhookTriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Webhook Trigger",
                "wh_abcdefabcdefabcdefabcdefabcdefab",
                "super-secret-token-1234",
                true
        );
        webhookExecutionStore.register(workflowId, triggerDefinition);
    }

    @Test
    void shouldAcceptWebhookRequestWithValidSecret() throws Exception {
        WebhookPayloadRequest request = new WebhookPayloadRequest(
                "super-secret-token-1234",
                objectMapper.createObjectNode().put("event", "order.created")
        );

        mockMvc.perform(post("/api/v1/webhooks/{workflowId}", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.workflowId").value(workflowId.toString()))
                .andExpect(jsonPath("$.executionRequestId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void shouldRejectWebhookRequestWithInvalidSecret() throws Exception {
        WebhookPayloadRequest request = new WebhookPayloadRequest(
                "invalid-secret",
                objectMapper.createObjectNode().put("event", "order.created")
        );

        mockMvc.perform(post("/api/v1/webhooks/{workflowId}", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}


