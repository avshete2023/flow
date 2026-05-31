package com.flow.connector.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flow.connector.registry.InMemoryTriggerRegistry;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TriggerValidationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private TriggerValidationService triggerValidationService;

    @BeforeEach
    void setUp() {
        InMemoryTriggerRegistry triggerRegistry = new InMemoryTriggerRegistry(List.of(
                new SchedulerTriggerHandler(),
                new WebhookTriggerHandler()
        ));
        triggerValidationService = new TriggerValidationService(triggerRegistry);
    }

    @Test
    void shouldValidateSchedulerTriggerSuccessfully() {
        ObjectNode configuration = objectMapper.createObjectNode()
                .put("cronExpression", "0 */5 * * * *")
                .put("timezone", "UTC")
                .put("enabled", true);

        TriggerDefinition triggerDefinition = new TriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TriggerType.SCHEDULER,
                "Scheduler Trigger",
                configuration,
                true
        );

        TriggerValidationModel result = triggerValidationService.validate(triggerDefinition);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldValidateWebhookTriggerSuccessfully() {
        ObjectNode configuration = objectMapper.createObjectNode()
                .put("webhookIdentifier", "wh_1234567890abcdef1234567890abcdef")
                .put("secretToken", "super-secret-token-1234")
                .put("active", true);

        TriggerDefinition triggerDefinition = new TriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TriggerType.WEBHOOK,
                "Webhook Trigger",
                configuration,
                true
        );

        TriggerValidationModel result = triggerValidationService.validate(triggerDefinition);

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldReturnFailureForUnsupportedTriggerType() {
        TriggerDefinition triggerDefinition = new TriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TriggerType.EVENT,
                "Event Trigger",
                objectMapper.createObjectNode().put("topic", "order.created"),
                true
        );

        TriggerValidationModel result = triggerValidationService.validate(triggerDefinition);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Trigger type is not supported: EVENT");
    }

    @Test
    void shouldReturnFailureWhenDefinitionIsNull() {
        TriggerValidationModel result = triggerValidationService.validate(null);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Trigger definition is required");
    }
}

