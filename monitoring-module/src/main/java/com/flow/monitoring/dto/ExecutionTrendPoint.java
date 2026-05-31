package com.flow.monitoring.dto;

import java.time.LocalDate;

/**
 * Lightweight trend point for dashboard charts.
 */
public record ExecutionTrendPoint(
        LocalDate day,
        long totalExecutions
) {
}

