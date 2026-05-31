package com.flow.execution.service;

import com.flow.execution.domain.model.ExecutionStatus;
import com.flow.execution.domain.repository.WorkflowExecutionRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only execution aggregations exposed to monitoring module.
 */
@Service
public class ExecutionMonitoringQueryService {

    private final WorkflowExecutionRepository workflowExecutionRepository;

    public ExecutionMonitoringQueryService(ObjectProvider<WorkflowExecutionRepository> workflowExecutionRepositoryProvider) {
        this.workflowExecutionRepository = workflowExecutionRepositoryProvider.getIfAvailable();
    }

    @Transactional(readOnly = true)
    public ExecutionStatistics getExecutionStatistics() {
        if (workflowExecutionRepository == null) {
            return new ExecutionStatistics(0, 0, 0, 0, 0, 0);
        }

        return new ExecutionStatistics(
                workflowExecutionRepository.count(),
                workflowExecutionRepository.countByStatus(ExecutionStatus.COMPLETED),
                workflowExecutionRepository.countByStatus(ExecutionStatus.FAILED),
                workflowExecutionRepository.countByStatus(ExecutionStatus.RUNNING),
                workflowExecutionRepository.countByStatus(ExecutionStatus.DLQ),
                workflowExecutionRepository.countByRetryCountGreaterThan(0)
        );
    }
}



