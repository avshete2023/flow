package com.flow.execution.queue;

import static org.mockito.Mockito.verify;

import com.flow.execution.ExecutionModuleTestApplication;
import com.flow.execution.orchestrator.ExecutionOrchestrator;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = ExecutionModuleTestApplication.class,
        properties = "spring.rabbitmq.listener.simple.auto-startup=false"
)
@ActiveProfiles("test")
class ExecutionConsumerIntegrationTest {

    @Autowired
    private ExecutionConsumer executionConsumer;

    @MockBean
    private ExecutionOrchestrator executionOrchestrator;

    @Test
    void shouldTriggerOrchestratorWhenExecutionMessageIsConsumed() {
        ExecutionRequestMessage request = new ExecutionRequestMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "corr-7008",
                LocalDateTime.now()
        );

        executionConsumer.consume(request);

        verify(executionOrchestrator).orchestrate(request);
    }
}



