package com.flow.execution.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flow.execution.domain.entity.WorkflowExecution;
import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.service.ManualRetryService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExecutionRetryController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExecutionRetryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManualRetryService manualRetryService;

    @Test
    void shouldReturnAcceptedResponseForManualRetry() throws Exception {
        UUID originalExecutionId = UUID.randomUUID();
        UUID newExecutionId = UUID.randomUUID();

        WorkflowExecution retryExecution = new WorkflowExecution(
                newExecutionId,
                UUID.randomUUID(),
                ExecutionStatus.PENDING,
                "corr-api",
                "{}"
        );

        when(manualRetryService.manualRetry(eq(originalExecutionId))).thenReturn(retryExecution);

        mockMvc.perform(post("/api/v1/executions/{executionId}/retry", originalExecutionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.executionId").value(newExecutionId.toString()))
                .andExpect(jsonPath("$.retriedFromExecutionId").value(originalExecutionId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}


