package com.flow.monitoring.metrics;

import com.flow.execution.queue.ExecutionQueueNames;
import com.flow.execution.service.ExecutionMonitoringQueryService;
import com.flow.execution.service.ExecutionStatistics;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import java.util.Optional;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Registers custom execution and queue metrics.
 */
@Component
public class ExecutionMetricsBinder implements MeterBinder {

    private final ExecutionMonitoringQueryService executionMonitoringQueryService;
    private final ObjectProvider<RabbitAdmin> rabbitAdminProvider;

    public ExecutionMetricsBinder(
            ExecutionMonitoringQueryService executionMonitoringQueryService,
            ObjectProvider<RabbitAdmin> rabbitAdminProvider
    ) {
        this.executionMonitoringQueryService = executionMonitoringQueryService;
        this.rabbitAdminProvider = rabbitAdminProvider;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("flow.executions.total", this, binder -> binder.statistics().totalExecutions())
                .description("Total executions")
                .register(registry);
        Gauge.builder("flow.executions.failed", this, binder -> binder.statistics().failedExecutions())
                .description("Failed executions")
                .register(registry);
        Gauge.builder("flow.executions.retries", this, binder -> binder.statistics().retriedExecutions())
                .description("Executions retried at least once")
                .register(registry);

        Gauge.builder("flow.queue.depth", this, binder -> binder.queueDepth(ExecutionQueueNames.WORKFLOW_EXECUTION_QUEUE))
                .tag("queue", ExecutionQueueNames.WORKFLOW_EXECUTION_QUEUE)
                .description("Execution queue depth")
                .register(registry);
        Gauge.builder("flow.queue.depth", this, binder -> binder.queueDepth(ExecutionQueueNames.WORKFLOW_RETRY_QUEUE))
                .tag("queue", ExecutionQueueNames.WORKFLOW_RETRY_QUEUE)
                .description("Retry queue depth")
                .register(registry);
        Gauge.builder("flow.queue.depth", this, binder -> binder.queueDepth(ExecutionQueueNames.WORKFLOW_DLQ_QUEUE))
                .tag("queue", ExecutionQueueNames.WORKFLOW_DLQ_QUEUE)
                .description("DLQ depth")
                .register(registry);
    }

    private ExecutionStatistics statistics() {
        return executionMonitoringQueryService.getExecutionStatistics();
    }

    private double queueDepth(String queueName) {
        RabbitAdmin rabbitAdmin = rabbitAdminProvider.getIfAvailable();
        if (rabbitAdmin == null) {
            return -1;
        }

        Object queueProperties = rabbitAdmin.getQueueProperties(queueName);
        if (!(queueProperties instanceof java.util.Properties properties)) {
            return -1;
        }

        return Optional.ofNullable(properties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT))
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast)
                .map(Integer::doubleValue)
                .orElse(-1d);
    }
}



