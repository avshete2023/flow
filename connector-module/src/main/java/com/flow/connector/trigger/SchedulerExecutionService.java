package com.flow.connector.trigger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

/**
 * Service that detects due scheduler triggers and publishes execution requests.
 */
@Service
public class SchedulerExecutionService {

    private final SchedulerExecutionPublisher schedulerExecutionPublisher;
    private final Map<UUID, SchedulerExecutionRegistration> registrationsByWorkflowId = new ConcurrentHashMap<>();
    private final Map<UUID, ZonedDateTime> lastScheduledRunByWorkflowId = new ConcurrentHashMap<>();

    public SchedulerExecutionService(SchedulerExecutionPublisher schedulerExecutionPublisher) {
        this.schedulerExecutionPublisher = schedulerExecutionPublisher;
    }

    public TriggerValidationModel register(SchedulerExecutionRegistration registration) {
        if (registration == null) {
            return TriggerValidationModel.failure(List.of("Scheduler registration is required"));
        }

        TriggerValidationModel registrationValidation = registration.validate();
        if (!registrationValidation.valid()) {
            return registrationValidation;
        }

        TriggerValidationModel triggerValidation = validateTrigger(registration.triggerDefinition());
        if (!triggerValidation.valid()) {
            return triggerValidation;
        }

        registrationsByWorkflowId.put(registration.workflowId(), registration);
        lastScheduledRunByWorkflowId.remove(registration.workflowId());
        return TriggerValidationModel.success();
    }

    public List<SchedulerExecutionRequest> triggerEligibleWorkflows(LocalDateTime now) {
        List<SchedulerExecutionRequest> published = new ArrayList<>();
        for (SchedulerExecutionRegistration registration : registrationsByWorkflowId.values()) {
            ZonedDateTime dueRun = resolveDueRun(registration, now);
            if (dueRun == null) {
                continue;
            }

            SchedulerExecutionRequest request = new SchedulerExecutionRequest(
                    UUID.randomUUID(),
                    registration.workflowId(),
                    registration.workflowVersionId(),
                    buildCorrelationId(registration.workflowId(), dueRun),
                    now
            );

            schedulerExecutionPublisher.publish(request);
            lastScheduledRunByWorkflowId.put(registration.workflowId(), dueRun);
            published.add(request);
        }
        return List.copyOf(published);
    }

    private TriggerValidationModel validateTrigger(SchedulerTriggerDefinition triggerDefinition) {
        List<String> errors = new ArrayList<>();

        if (triggerDefinition.cronExpression() == null || triggerDefinition.cronExpression().isBlank()) {
            errors.add("Cron expression is required");
        } else {
            try {
                CronExpression.parse(triggerDefinition.cronExpression());
            } catch (IllegalArgumentException exception) {
                errors.add("Cron expression is invalid");
            }
        }

        if (triggerDefinition.timezone() == null || triggerDefinition.timezone().isBlank()) {
            errors.add("Timezone is required");
        } else {
            try {
                ZoneId.of(triggerDefinition.timezone());
            } catch (Exception exception) {
                errors.add("Timezone is invalid");
            }
        }

        return errors.isEmpty() ? TriggerValidationModel.success() : TriggerValidationModel.failure(errors);
    }

    private ZonedDateTime resolveDueRun(SchedulerExecutionRegistration registration, LocalDateTime now) {
        SchedulerTriggerDefinition trigger = registration.triggerDefinition();
        if (!trigger.enabled()) {
            return null;
        }

        CronExpression cronExpression;
        ZoneId zoneId;
        try {
            cronExpression = CronExpression.parse(trigger.cronExpression());
            zoneId = ZoneId.of(trigger.timezone());
        } catch (Exception exception) {
            return null;
        }

        ZonedDateTime nowInZone = now.atZone(zoneId);
        ZonedDateTime anchor = lastScheduledRunByWorkflowId.containsKey(registration.workflowId())
                ? lastScheduledRunByWorkflowId.get(registration.workflowId())
                : nowInZone.minusSeconds(1);

        ZonedDateTime nextRun = cronExpression.next(anchor);
        if (nextRun == null || nextRun.isAfter(nowInZone)) {
            return null;
        }
        return nextRun;
    }

    private String buildCorrelationId(UUID workflowId, ZonedDateTime dueRun) {
        return "scheduler-" + workflowId + "-" + dueRun.toInstant().toEpochMilli();
    }
}

