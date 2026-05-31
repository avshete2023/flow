package com.flow.execution.queue;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
@Import(ExecutionQueueConfiguration.class)
class ExecutionQueueConfigurationTest {

    @Autowired
    @Qualifier("workflowExecutionQueue")
    private Queue executionQueue;

    @Autowired
    @Qualifier("workflowRetryQueue")
    private Queue retryQueue;

    @Autowired
    @Qualifier("workflowDlqQueue")
    private Queue dlqQueue;

    @Autowired
    @Qualifier("workflowExecutionExchange")
    private DirectExchange exchange;

    @Autowired
    @Qualifier("workflowExecutionBinding")
    private Binding executionBinding;

    @Autowired
    @Qualifier("workflowRetryBinding")
    private Binding retryBinding;

    @Autowired
    @Qualifier("workflowDlqBinding")
    private Binding dlqBinding;

    @Test
    void shouldInitializeExecutionQueues() {
        assertThat(executionQueue.getName()).isEqualTo(ExecutionQueueNames.WORKFLOW_EXECUTION_QUEUE);
        assertThat(retryQueue.getName()).isEqualTo(ExecutionQueueNames.WORKFLOW_RETRY_QUEUE);
        assertThat(dlqQueue.getName()).isEqualTo(ExecutionQueueNames.WORKFLOW_DLQ_QUEUE);

        assertThat(executionQueue.isDurable()).isTrue();
        assertThat(retryQueue.isDurable()).isTrue();
        assertThat(dlqQueue.isDurable()).isTrue();

        assertThat(exchange.getName()).isEqualTo(ExecutionQueueNames.WORKFLOW_EXECUTION_EXCHANGE);
        assertThat(executionBinding.getRoutingKey()).isEqualTo(ExecutionQueueNames.WORKFLOW_EXECUTION_ROUTING_KEY);
        assertThat(retryBinding.getRoutingKey()).isEqualTo(ExecutionQueueNames.WORKFLOW_RETRY_ROUTING_KEY);
        assertThat(dlqBinding.getRoutingKey()).isEqualTo(ExecutionQueueNames.WORKFLOW_DLQ_ROUTING_KEY);
    }
}


