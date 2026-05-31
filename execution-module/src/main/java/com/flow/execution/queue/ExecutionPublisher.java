package com.flow.execution.queue;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes execution requests to RabbitMQ.
 */
@Service
public class ExecutionPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ExecutionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public ExecutionRequestMessage publishExecutionRequest(UUID workflowVersionId, String correlationId) {
        ExecutionRequestMessage request = new ExecutionRequestMessage(
                UUID.randomUUID(),
                workflowVersionId,
                correlationId,
                LocalDateTime.now()
        );

        MessagePostProcessor correlationPostProcessor = message -> {
            if (correlationId != null && !correlationId.isBlank()) {
                message.getMessageProperties().setCorrelationId(correlationId);
            }
            return message;
        };

        rabbitTemplate.convertAndSend(
                ExecutionQueueNames.WORKFLOW_EXECUTION_QUEUE,
                request,
                correlationPostProcessor
        );

        return request;
    }
}



