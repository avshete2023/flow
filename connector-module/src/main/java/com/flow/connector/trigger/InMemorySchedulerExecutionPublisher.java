package com.flow.connector.trigger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory scheduler execution publisher used by tests and local runs.
 */
public class InMemorySchedulerExecutionPublisher implements SchedulerExecutionPublisher {

    private final List<SchedulerExecutionRequest> publishedRequests = new CopyOnWriteArrayList<>();

    @Override
    public void publish(SchedulerExecutionRequest request) {
        publishedRequests.add(request);
    }

    public List<SchedulerExecutionRequest> publishedRequests() {
        return List.copyOf(publishedRequests);
    }
}


