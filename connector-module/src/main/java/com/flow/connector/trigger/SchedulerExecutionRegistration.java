package com.flow.connector.trigger;

import java.util.UUID;

/**
 * Scheduler registration for a workflow version.
 */
public record SchedulerExecutionRegistration(
        UUID workflowId,
        UUID workflowVersionId,
        SchedulerTriggerDefinition triggerDefinition
) {

    public TriggerValidationModel validate() {
        if (workflowId == null) {
            return TriggerValidationModel.failure(java.util.List.of("Workflow id is required"));
        }
        if (workflowVersionId == null) {
            return TriggerValidationModel.failure(java.util.List.of("Workflow version id is required"));
        }
        if (triggerDefinition == null) {
            return TriggerValidationModel.failure(java.util.List.of("Scheduler trigger definition is required"));
        }
        return TriggerValidationModel.success();
    }
}

