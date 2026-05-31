package com.flow.execution.service;

/**
 * Aggregated execution statistics for monitoring dashboards and metrics.
 */
public record ExecutionStatistics(
        long totalExecutions,
        long successfulExecutions,
        long failedExecutions,
        long runningExecutions,
        long dlqExecutions,
        long retriedExecutions
) {
}

