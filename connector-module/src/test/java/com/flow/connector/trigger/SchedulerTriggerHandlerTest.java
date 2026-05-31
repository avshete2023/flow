package com.flow.connector.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerTriggerHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchedulerTriggerHandler schedulerTriggerHandler = new SchedulerTriggerHandler();
    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldSupportSchedulerTypeAndEnabledFlagAndTimezone() {
        SchedulerTriggerDefinition definition = new SchedulerTriggerDefinition("0 */5 * * * *", "UTC", true);

        Set<?> violations = validator.validate(definition);
        TriggerValidationModel validationModel = schedulerTriggerHandler.validateConfiguration(objectMapper.createObjectNode()
                .put("cronExpression", definition.cronExpression())
                .put("timezone", definition.timezone())
                .put("enabled", definition.enabled()));

        assertThat(schedulerTriggerHandler.getSupportedType()).isEqualTo(TriggerType.SCHEDULER);
        assertThat(violations).isEmpty();
        assertThat(definition.enabled()).isTrue();
        assertThat(validationModel.valid()).isTrue();
    }

    @Test
    void shouldRejectMissingCronExpression() {
        TriggerValidationModel validationModel = schedulerTriggerHandler.validateConfiguration(objectMapper.createObjectNode()
                .put("timezone", "UTC"));

        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Cron expression is required");
    }

    @Test
    void shouldRejectInvalidCronExpression() {
        TriggerValidationModel validationModel = schedulerTriggerHandler.validateConfiguration(objectMapper.createObjectNode()
                .put("cronExpression", "invalid-cron")
                .put("timezone", "UTC"));

        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Cron expression is invalid");
    }

    @Test
    void shouldRejectInvalidTimezone() {
        TriggerValidationModel validationModel = schedulerTriggerHandler.validateConfiguration(objectMapper.createObjectNode()
                .put("cronExpression", "0 */5 * * * *")
                .put("timezone", "Invalid/Zone"));

        assertThat(validationModel.valid()).isFalse();
        assertThat(validationModel.errors()).contains("Timezone is invalid");
    }
}


