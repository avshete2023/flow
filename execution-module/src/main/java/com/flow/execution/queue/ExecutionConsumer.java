package com.flow.execution.queue;

import com.flow.execution.orchestrator.ExecutionOrchestrator;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes execution queue messages and forwards them to the orchestrator.
 */
@Component
public class ExecutionConsumer {

    private final ExecutionOrchestrator executionOrchestrator;

    public ExecutionConsumer(ExecutionOrchestrator executionOrchestrator) {
        this.executionOrchestrator = executionOrchestrator;
    }

    @RabbitListener(queues = ExecutionQueueNames.WORKFLOW_EXECUTION_QUEUE)
    public void consume(ExecutionRequestMessage request) {
        executionOrchestrator.orchestrate(request);
    }
}

