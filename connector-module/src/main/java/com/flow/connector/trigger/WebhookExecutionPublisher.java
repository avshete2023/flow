package com.flow.connector.trigger;

/**
 * Abstraction for publishing webhook-triggered execution requests.
 */
public interface WebhookExecutionPublisher {

    void publish(WebhookExecutionRequest request);
}

