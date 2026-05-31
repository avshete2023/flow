package com.flow.connector.trigger;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Trigger handler for scheduler trigger configuration.
 */
@Component
public class SchedulerTriggerHandler implements TriggerHandler {

    // Basic 6-field cron shape used by project examples: sec min hour day month weekday
    private static final String SIX_FIELD_CRON_PATTERN =
            "^\\s*[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s*$";

    @Override
    public TriggerType getSupportedType() {
        return TriggerType.SCHEDULER;
    }

    @Override
    public TriggerValidationModel validateConfiguration(JsonNode configuration) {
        List<String> errors = new ArrayList<>();

        if (configuration == null || configuration.isNull() || !configuration.isObject()) {
            errors.add("Scheduler configuration must be a JSON object");
            return TriggerValidationModel.failure(errors);
        }

        String cronExpression = text(configuration.get("cronExpression"));
        if (cronExpression == null) {
            errors.add("Cron expression is required");
        } else if (!cronExpression.matches(SIX_FIELD_CRON_PATTERN)) {
            errors.add("Cron expression is invalid");
        }

        String timezone = text(configuration.get("timezone"));
        if (timezone == null) {
            errors.add("Timezone is required");
        } else {
            try {
                ZoneId zoneId = ZoneId.of(timezone);
                if (zoneId.getId().isBlank()) {
                    errors.add("Timezone is invalid");
                }
            } catch (DateTimeException exception) {
                errors.add("Timezone is invalid");
            }
        }

        return errors.isEmpty() ? TriggerValidationModel.success() : TriggerValidationModel.failure(errors);
    }

    private String text(JsonNode node) {
        if (node == null || !node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }
}


