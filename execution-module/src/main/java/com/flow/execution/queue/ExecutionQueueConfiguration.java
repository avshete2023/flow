package com.flow.execution.queue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ queue declarations for workflow execution flow.
 */
@Configuration
public class ExecutionQueueConfiguration {

    @Bean
    public DirectExchange workflowExecutionExchange() {
        return new DirectExchange(ExecutionQueueNames.WORKFLOW_EXECUTION_EXCHANGE, true, false);
    }

    @Bean
    public Queue workflowExecutionQueue() {
        return new Queue(ExecutionQueueNames.WORKFLOW_EXECUTION_QUEUE, true);
    }

    @Bean
    public Queue workflowRetryQueue() {
        return new Queue(ExecutionQueueNames.WORKFLOW_RETRY_QUEUE, true);
    }

    @Bean
    public Queue workflowDlqQueue() {
        return new Queue(ExecutionQueueNames.WORKFLOW_DLQ_QUEUE, true);
    }

    @Bean
    public Binding workflowExecutionBinding(
            @Qualifier("workflowExecutionQueue") Queue workflowExecutionQueue,
            DirectExchange workflowExecutionExchange
    ) {
        return BindingBuilder.bind(workflowExecutionQueue)
                .to(workflowExecutionExchange)
                .with(ExecutionQueueNames.WORKFLOW_EXECUTION_ROUTING_KEY);
    }

    @Bean
    public Binding workflowRetryBinding(
            @Qualifier("workflowRetryQueue") Queue workflowRetryQueue,
            DirectExchange workflowExecutionExchange
    ) {
        return BindingBuilder.bind(workflowRetryQueue)
                .to(workflowExecutionExchange)
                .with(ExecutionQueueNames.WORKFLOW_RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding workflowDlqBinding(
            @Qualifier("workflowDlqQueue") Queue workflowDlqQueue,
            DirectExchange workflowExecutionExchange
    ) {
        return BindingBuilder.bind(workflowDlqQueue)
                .to(workflowExecutionExchange)
                .with(ExecutionQueueNames.WORKFLOW_DLQ_ROUTING_KEY);
    }
}



