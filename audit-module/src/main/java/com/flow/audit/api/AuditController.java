package com.flow.audit.api;

import com.flow.audit.dto.AuditEventPageResponse;
import com.flow.audit.dto.AuditEventResponse;
import com.flow.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Read-only audit query endpoints.
 */
@RestController
@RequestMapping("/api/v1/audit")
@Validated
@Tag(name = "Audit", description = "Audit query APIs")
public class AuditController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "occurredAt",
            "eventType",
            "resourceType",
            "actorId",
            "id"
    );

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "List audit events")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<AuditEventPageResponse> listAuditEvents(
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<AuditEventResponse> responsePage = auditService.listAuditEvents(
                actorId,
                eventType,
                resourceType,
                startDate,
                endDate,
                pageable
        );

        AuditEventPageResponse response = new AuditEventPageResponse(
                responsePage.getContent(),
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{auditId}")
    @Operation(summary = "Get audit event")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<AuditEventResponse> getAuditEvent(@PathVariable UUID auditId) {
        try {
            return ResponseEntity.ok(auditService.getAuditEvent(auditId));
        } catch (AuditService.AuditEventNotFoundException ex) {
            throw new ResponseStatusException(NOT_FOUND, ex.getMessage());
        }
    }

    private Sort parseSort(String sort) {
        String[] tokens = sort == null ? new String[0] : sort.split(",", 2);
        String field = tokens.length > 0 && !tokens[0].isBlank() ? tokens[0].trim() : "occurredAt";
        String direction = tokens.length > 1 && !tokens[1].isBlank() ? tokens[1].trim() : "desc";

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new ResponseStatusException(BAD_REQUEST, "Unsupported sort field: " + field);
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Unsupported sort direction: " + direction);
        }

        return Sort.by(new Sort.Order(sortDirection, field));
    }
}

