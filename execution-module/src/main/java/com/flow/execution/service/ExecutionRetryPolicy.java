package com.flow.execution.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Retry policy for failed workflow executions.
 */
@ConfigurationProperties(prefix = "flow.execution.retry")
public record ExecutionRetryPolicy(
        @DefaultValue("3") int maxRetries,
        @DefaultValue("1000") long initialBackoffMillis,
        @DefaultValue("2.0") double backoffMultiplier
) {

    public ExecutionRetryPolicy {
        if (maxRetries < 1) {
            throw new IllegalArgumentException("maxRetries must be at least 1");
        }
        if (initialBackoffMillis < 1) {
            throw new IllegalArgumentException("initialBackoffMillis must be at least 1");
        }
        if (backoffMultiplier < 1.0d) {
            throw new IllegalArgumentException("backoffMultiplier must be at least 1.0");
        }
    }

    public long backoffMillisForAttempt(int attempt) {
        if (attempt < 1) {
            throw new IllegalArgumentException("attempt must be >= 1");
        }
        if (attempt == 1) {
            return initialBackoffMillis;
        }
        return Math.round(initialBackoffMillis * Math.pow(backoffMultiplier, attempt - 1));
    }
}


