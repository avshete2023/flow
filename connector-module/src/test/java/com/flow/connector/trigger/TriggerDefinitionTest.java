package com.flow.connector.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TriggerDefinitionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldSerializeAndDeserializeTriggerDefinitionWithJsonConfiguration() throws Exception {
        JsonNode configuration = objectMapper.readTree("{\"cron\":\"0 */5 * * * *\",\"timezone\":\"UTC\"}");
        TriggerDefinition definition = new TriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TriggerType.SCHEDULER,
                "Scheduler Trigger",
                configuration,
                true
        );

        String json = objectMapper.writeValueAsString(definition);
        TriggerDefinition restored = objectMapper.readValue(json, TriggerDefinition.class);

        assertThat(restored.id()).isEqualTo(definition.id());
        assertThat(restored.workflowVersionId()).isEqualTo(definition.workflowVersionId());
        assertThat(restored.triggerType()).isEqualTo(TriggerType.SCHEDULER);
        assertThat(restored.configuration().get("cron").asText()).isEqualTo("0 */5 * * * *");
        assertThat(restored.enabled()).isTrue();
    }

    @Test
    void shouldPassBeanValidationForValidTriggerDefinition() {
        JsonNode configuration = objectMapper.createObjectNode().put("path", "/events/order-created");
        TriggerDefinition definition = new TriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TriggerType.WEBHOOK,
                "Webhook Trigger",
                configuration,
                true
        );

        Set<?> violations = validator.validate(definition);
        TriggerValidationModel validationModel = definition.validate();

        assertThat(violations).isEmpty();
        assertThat(validationModel.valid()).isTrue();
        assertThat(validationModel.errors()).isEmpty();
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsAreMissing() {
        TriggerDefinition invalid = new TriggerDefinition(
                null,
                null,
                null,
                "   ",
                objectMapper.createObjectNode(),
                true
        );

        Set<?> violations = validator.validate(invalid);
        TriggerValidationModel validationModel = invalid.validate();

        assertThat(violations).isNotEmpty();
        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Trigger id is required");
        assertThat(validationModel.errors()).contains("Workflow version id is required");
        assertThat(validationModel.errors()).contains("Trigger type is required");
        assertThat(validationModel.errors()).contains("Trigger name is required");
        assertThat(validationModel.errors()).contains("Trigger configuration must be a non-empty JSON object");
    }

    @Test
    void shouldFailValidationWhenConfigurationIsNotJsonObject() throws Exception {
        JsonNode arrayConfiguration = objectMapper.readTree("[1,2,3]");
        TriggerDefinition invalid = new TriggerDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TriggerType.EVENT,
                "Event Trigger",
                arrayConfiguration,
                true
        );

        TriggerValidationModel validationModel = invalid.validate();

        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Trigger configuration must be a non-empty JSON object");
    }
}

