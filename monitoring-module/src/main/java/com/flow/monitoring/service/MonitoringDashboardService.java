package com.flow.monitoring.service;

import com.flow.execution.service.ExecutionMonitoringQueryService;
import com.flow.execution.service.ExecutionStatistics;
import com.flow.monitoring.dto.DashboardResponse;
import com.flow.monitoring.dto.ExecutionTrendPoint;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides aggregated execution statistics for monitoring dashboard.
 */
@Service
public class MonitoringDashboardService {

    private final ExecutionMonitoringQueryService executionMonitoringQueryService;

    public MonitoringDashboardService(ExecutionMonitoringQueryService executionMonitoringQueryService) {
        this.executionMonitoringQueryService = executionMonitoringQueryService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        ExecutionStatistics statistics = executionMonitoringQueryService.getExecutionStatistics();

        // Placeholder trend/workflow series until historical aggregation stories are implemented.
        List<ExecutionTrendPoint> trends = List.of(new ExecutionTrendPoint(LocalDate.now(), statistics.totalExecutions()));
        Map<String, Long> workflowStatistics = Map.of("total", statistics.totalExecutions());

        return new DashboardResponse(
                statistics.totalExecutions(),
                statistics.successfulExecutions(),
                statistics.failedExecutions(),
                statistics.runningExecutions(),
                statistics.dlqExecutions(),
                trends,
                workflowStatistics
        );
    }
}



