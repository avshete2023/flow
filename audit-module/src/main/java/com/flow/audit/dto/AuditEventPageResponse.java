package com.flow.audit.dto;

import java.util.List;

/**
 * Paginated audit events response payload.
 */
public record AuditEventPageResponse(
        List<AuditEventResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}

