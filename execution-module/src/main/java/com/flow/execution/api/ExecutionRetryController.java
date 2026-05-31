package com.flow.execution.api;

import com.flow.execution.dto.RetryExecutionResponse;
import com.flow.execution.service.ManualRetryService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API for user-triggered execution retries.
 */
@RestController
@RequestMapping("/api/v1/executions")
public class ExecutionRetryController {

    private final ManualRetryService manualRetryService;

    public ExecutionRetryController(ManualRetryService manualRetryService) {
        this.manualRetryService = manualRetryService;
    }

    @PostMapping("/{executionId}/retry")
    public ResponseEntity<RetryExecutionResponse> retryExecution(@PathVariable UUID executionId) {
        var retriedExecution = manualRetryService.manualRetry(executionId);
        RetryExecutionResponse response = new RetryExecutionResponse(
                retriedExecution.getId(),
                executionId,
                retriedExecution.getStatus()
        );
        return ResponseEntity.accepted().body(response);
    }
}

