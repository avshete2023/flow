package com.flow.monitoring.dto;

import java.util.List;
import java.util.Map;

/**
 * Monitoring dashboard payload.
 */
public record DashboardResponse(
        long totalExecutions,
        long successfulExecutions,
        long failedExecutions,
        long runningExecutions,
        long dlqExecutions,
        List<ExecutionTrendPoint> executionTrends,
        Map<String, Long> workflowStatistics
) {
}

