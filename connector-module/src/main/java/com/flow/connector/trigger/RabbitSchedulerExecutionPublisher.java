package com.flow.connector.trigger;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ publisher for scheduler-triggered execution requests.
 */
@Service
public class RabbitSchedulerExecutionPublisher implements SchedulerExecutionPublisher {

    static final String WORKFLOW_EXECUTION_EXCHANGE = "workflow.execution.exchange";
    static final String WORKFLOW_EXECUTION_ROUTING_KEY = "workflow.execution";

    private final RabbitTemplate rabbitTemplate;

    public RabbitSchedulerExecutionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(SchedulerExecutionRequest request) {
        MessagePostProcessor correlationPostProcessor = message -> {
            if (request.correlationId() != null && !request.correlationId().isBlank()) {
                message.getMessageProperties().setCorrelationId(request.correlationId());
            }
            return message;
        };

        rabbitTemplate.convertAndSend(
                WORKFLOW_EXECUTION_EXCHANGE,
                WORKFLOW_EXECUTION_ROUTING_KEY,
                request,
                correlationPostProcessor
        );
    }
}

