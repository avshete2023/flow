package com.flow.connector.trigger;

import jakarta.validation.constraints.NotBlank;

/**
 * Scheduler trigger-specific configuration model.
 */
public record SchedulerTriggerDefinition(
        @NotBlank(message = "Cron expression is required")
        String cronExpression,

        @NotBlank(message = "Timezone is required")
        String timezone,

        boolean enabled
) {
}

