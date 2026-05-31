package com.flow.execution.domain.model;

/**
 * Execution lifecycle states.
 */
public enum ExecutionStatus {
    PENDING,
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    TIMED_OUT,
    DLQ
}

