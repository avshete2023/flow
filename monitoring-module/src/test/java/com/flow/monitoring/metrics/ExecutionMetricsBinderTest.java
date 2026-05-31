package com.flow.monitoring.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.flow.execution.queue.ExecutionQueueNames;
import com.flow.execution.service.ExecutionMonitoringQueryService;
import com.flow.execution.service.ExecutionStatistics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.ObjectProvider;

class ExecutionMetricsBinderTest {

    @Test
    void shouldExposeExecutionAndQueueMetrics() {
        ExecutionMonitoringQueryService queryService = org.mockito.Mockito.mock(ExecutionMonitoringQueryService.class);
        when(queryService.getExecutionStatistics()).thenReturn(new ExecutionStatistics(12, 8, 2, 1, 1, 3));

        RabbitAdmin rabbitAdmin = org.mockito.Mockito.mock(RabbitAdmin.class);
        Properties queueProperties = new Properties();
        queueProperties.put(RabbitAdmin.QUEUE_MESSAGE_COUNT, 9);
        when(rabbitAdmin.getQueueProperties(ExecutionQueueNames.WORKFLOW_EXECUTION_QUEUE)).thenReturn(queueProperties);
        when(rabbitAdmin.getQueueProperties(ExecutionQueueNames.WORKFLOW_RETRY_QUEUE)).thenReturn(queueProperties);
        when(rabbitAdmin.getQueueProperties(ExecutionQueueNames.WORKFLOW_DLQ_QUEUE)).thenReturn(queueProperties);

        @SuppressWarnings("unchecked")
        ObjectProvider<RabbitAdmin> provider = org.mockito.Mockito.mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(rabbitAdmin);

        ExecutionMetricsBinder binder = new ExecutionMetricsBinder(queryService, provider);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        binder.bindTo(registry);

        assertThat(registry.get("flow.executions.total").gauge().value()).isEqualTo(12d);
        assertThat(registry.get("flow.executions.failed").gauge().value()).isEqualTo(2d);
        assertThat(registry.get("flow.executions.retries").gauge().value()).isEqualTo(3d);
        assertThat(registry.find("flow.queue.depth").tag("queue", ExecutionQueueNames.WORKFLOW_DLQ_QUEUE).gauge().value()).isEqualTo(9d);
    }
}

