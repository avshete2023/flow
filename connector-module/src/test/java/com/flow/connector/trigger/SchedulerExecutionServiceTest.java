package com.flow.connector.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerExecutionServiceTest {

    private InMemorySchedulerExecutionPublisher schedulerExecutionPublisher;
    private SchedulerExecutionService schedulerExecutionService;

    @BeforeEach
    void setUp() {
        schedulerExecutionPublisher = new InMemorySchedulerExecutionPublisher();
        schedulerExecutionService = new SchedulerExecutionService(schedulerExecutionPublisher);
    }

    @Test
    void shouldPublishExecutionRequestForEligibleSchedule() {
        SchedulerExecutionRegistration registration = new SchedulerExecutionRegistration(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new SchedulerTriggerDefinition("0 */5 * * * *", "UTC", true)
        );

        TriggerValidationModel registrationResult = schedulerExecutionService.register(registration);
        List<SchedulerExecutionRequest> fired = schedulerExecutionService.triggerEligibleWorkflows(
                LocalDateTime.of(2026, 6, 1, 10, 15, 0)
        );

        assertThat(registrationResult.valid()).isTrue();
        assertThat(fired).hasSize(1);
        assertThat(fired.get(0).workflowId()).isEqualTo(registration.workflowId());
        assertThat(fired.get(0).workflowVersionId()).isEqualTo(registration.workflowVersionId());
        assertThat(schedulerExecutionPublisher.publishedRequests()).hasSize(1);
    }

    @Test
    void shouldPreventDuplicateSchedulingForSameRunWindow() {
        SchedulerExecutionRegistration registration = new SchedulerExecutionRegistration(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new SchedulerTriggerDefinition("0 */5 * * * *", "UTC", true)
        );

        schedulerExecutionService.register(registration);

        LocalDateTime dueTime = LocalDateTime.of(2026, 6, 1, 10, 20, 0);
        List<SchedulerExecutionRequest> firstFire = schedulerExecutionService.triggerEligibleWorkflows(dueTime);
        List<SchedulerExecutionRequest> secondFire = schedulerExecutionService.triggerEligibleWorkflows(dueTime);

        assertThat(firstFire).hasSize(1);
        assertThat(secondFire).isEmpty();
        assertThat(schedulerExecutionPublisher.publishedRequests()).hasSize(1);
    }

    @Test
    void shouldRejectRegistrationWithInvalidCronExpression() {
        SchedulerExecutionRegistration registration = new SchedulerExecutionRegistration(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new SchedulerTriggerDefinition("invalid-cron", "UTC", true)
        );

        TriggerValidationModel result = schedulerExecutionService.register(registration);

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).contains("Cron expression is invalid");
    }

    @Test
    void shouldIgnoreDisabledSchedulesWhenCheckingEligibility() {
        SchedulerExecutionRegistration registration = new SchedulerExecutionRegistration(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new SchedulerTriggerDefinition("0 */5 * * * *", "UTC", false)
        );

        schedulerExecutionService.register(registration);
        List<SchedulerExecutionRequest> fired = schedulerExecutionService.triggerEligibleWorkflows(
                LocalDateTime.of(2026, 6, 1, 10, 25, 0)
        );

        assertThat(fired).isEmpty();
        assertThat(schedulerExecutionPublisher.publishedRequests()).isEmpty();
    }
}

