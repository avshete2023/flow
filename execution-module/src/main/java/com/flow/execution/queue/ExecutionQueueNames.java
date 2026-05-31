package com.flow.execution.queue;

/**
 * RabbitMQ queue names used by execution module.
 */
public final class ExecutionQueueNames {

    public static final String WORKFLOW_EXECUTION_EXCHANGE = "workflow.execution.exchange";

    public static final String WORKFLOW_EXECUTION_QUEUE = "workflow.execution";
    public static final String WORKFLOW_RETRY_QUEUE = "workflow.retry";
    public static final String WORKFLOW_DLQ_QUEUE = "workflow.dlq";

    public static final String WORKFLOW_EXECUTION_ROUTING_KEY = "workflow.execution";
    public static final String WORKFLOW_RETRY_ROUTING_KEY = "workflow.retry";
    public static final String WORKFLOW_DLQ_ROUTING_KEY = "workflow.dlq";

    private ExecutionQueueNames() {
    }
}


