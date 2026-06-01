package com.flow.connector.trigger;

/**
 * Abstraction for publishing scheduler-triggered execution requests.
 */
public interface SchedulerExecutionPublisher {

    void publish(SchedulerExecutionRequest request);
}

