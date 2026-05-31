package com.flow.connector.trigger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * In-memory publisher implementation used for local development and tests.
 */
@Component
public class InMemoryWebhookExecutionPublisher implements WebhookExecutionPublisher {

    private final List<WebhookExecutionRequest> publishedRequests = new CopyOnWriteArrayList<>();

    @Override
    public void publish(WebhookExecutionRequest request) {
        publishedRequests.add(request);
    }

    public List<WebhookExecutionRequest> publishedRequests() {
        return List.copyOf(publishedRequests);
    }
}

