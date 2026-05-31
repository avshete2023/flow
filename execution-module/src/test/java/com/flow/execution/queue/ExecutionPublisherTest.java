package com.flow.execution.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class ExecutionPublisherTest {

    @Test
    void shouldPublishExecutionRequestWithCorrelationId() {
        RabbitTemplate rabbitTemplate = org.mockito.Mockito.mock(RabbitTemplate.class);
        ExecutionPublisher executionPublisher = new ExecutionPublisher(rabbitTemplate);

        UUID workflowVersionId = UUID.randomUUID();
        String correlationId = "corr-2026";

        ExecutionRequestMessage published = executionPublisher.publishExecutionRequest(workflowVersionId, correlationId);

        assertThat(published.executionId()).isNotNull();
        assertThat(published.workflowVersionId()).isEqualTo(workflowVersionId);
        assertThat(published.correlationId()).isEqualTo(correlationId);
        assertThat(published.requestedAt()).isNotNull();

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        verify(rabbitTemplate).convertAndSend(
                eq(ExecutionQueueNames.WORKFLOW_EXECUTION_EXCHANGE),
                eq(ExecutionQueueNames.WORKFLOW_EXECUTION_ROUTING_KEY),
                eq(published),
                postProcessorCaptor.capture()
        );

        Message message = new Message(new byte[0], new MessageProperties());
        Message processed = postProcessorCaptor.getValue().postProcessMessage(message);

        assertThat(processed.getMessageProperties().getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    void shouldPublishExecutionRequestWithoutCorrelationHeaderWhenMissing() {
        RabbitTemplate rabbitTemplate = org.mockito.Mockito.mock(RabbitTemplate.class);
        ExecutionPublisher executionPublisher = new ExecutionPublisher(rabbitTemplate);

        ExecutionRequestMessage published = executionPublisher.publishExecutionRequest(UUID.randomUUID(), " ");

        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), eq(published), postProcessorCaptor.capture());

        Message message = new Message(new byte[0], new MessageProperties());
        Message processed = postProcessorCaptor.getValue().postProcessMessage(message);

        assertThat(processed.getMessageProperties().getCorrelationId()).isNull();
    }
}



