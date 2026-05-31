package com.flow.workflow.dto;

import java.util.List;

/**
 * Paginated workflow response payload.
 */
public record WorkflowPageResponse(
        List<WorkflowResponse> content,
        int page,
        int size,
        long totalElements
) {
}

