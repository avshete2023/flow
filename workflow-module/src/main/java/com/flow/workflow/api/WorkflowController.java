package com.flow.workflow.api;

import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.dto.CreateWorkflowRequest;
import com.flow.workflow.dto.PublishWorkflowRequest;
import com.flow.workflow.dto.PublishWorkflowResponse;
import com.flow.workflow.dto.UpdateWorkflowRequest;
import com.flow.workflow.dto.WorkflowPageResponse;
import com.flow.workflow.dto.WorkflowResponse;
import com.flow.workflow.service.WorkflowActivationService;
import com.flow.workflow.service.WorkflowCrudService;
import com.flow.workflow.service.WorkflowPublishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Workflow management API endpoints.
 */
@RestController
@RequestMapping("/api/v1/workflows")
@Tag(name = "Workflows", description = "Workflow lifecycle APIs")
public class WorkflowController {

    private final WorkflowCrudService workflowCrudService;
    private final WorkflowPublishService workflowPublishService;
    private final WorkflowActivationService workflowActivationService;

    public WorkflowController(
            WorkflowCrudService workflowCrudService,
            WorkflowPublishService workflowPublishService,
            WorkflowActivationService workflowActivationService
    ) {
        this.workflowCrudService = workflowCrudService;
        this.workflowPublishService = workflowPublishService;
        this.workflowActivationService = workflowActivationService;
    }

    @PostMapping
    @Operation(summary = "Create workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Workflow created",
                    content = @Content(schema = @Schema(implementation = WorkflowResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<WorkflowResponse> createWorkflow(
            Principal principal,
            @Valid @RequestBody CreateWorkflowRequest request
    ) {
        UUID ownerId = extractUserIdFromPrincipal(principal);
        WorkflowResponse response = workflowCrudService.createWorkflow(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{workflowId}")
    @Operation(summary = "Get workflow")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<WorkflowResponse> getWorkflow(
            Principal principal,
            @PathVariable UUID workflowId
    ) {
        UUID ownerId = extractUserIdFromPrincipal(principal);
        try {
            return ResponseEntity.ok(workflowCrudService.getWorkflow(ownerId, workflowId));
        } catch (WorkflowCrudService.WorkflowNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PutMapping("/{workflowId}")
    @Operation(summary = "Update workflow")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<WorkflowResponse> updateWorkflow(
            Principal principal,
            @PathVariable UUID workflowId,
            @Valid @RequestBody UpdateWorkflowRequest request
    ) {
        UUID ownerId = extractUserIdFromPrincipal(principal);
        try {
            return ResponseEntity.ok(workflowCrudService.updateWorkflow(ownerId, workflowId, request));
        } catch (WorkflowCrudService.WorkflowNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (WorkflowCrudService.WorkflowImmutableException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    @DeleteMapping("/{workflowId}")
    @Operation(summary = "Delete workflow")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<Void> deleteWorkflow(
            Principal principal,
            @PathVariable UUID workflowId
    ) {
        UUID ownerId = extractUserIdFromPrincipal(principal);
        try {
            workflowCrudService.deleteWorkflow(ownerId, workflowId);
            return ResponseEntity.noContent().build();
        } catch (WorkflowCrudService.WorkflowNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "List workflows")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<WorkflowPageResponse> listWorkflows(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) WorkflowStatus status
    ) {
        UUID ownerId = extractUserIdFromPrincipal(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<WorkflowResponse> responsePage = workflowCrudService.listWorkflows(ownerId, status, pageable);
        WorkflowPageResponse response = new WorkflowPageResponse(
                responsePage.getContent(),
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{workflowId}/publish")
    @Operation(summary = "Publish workflow")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<PublishWorkflowResponse> publishWorkflow(
            Principal principal,
            @PathVariable UUID workflowId,
            @Valid @RequestBody PublishWorkflowRequest request
    ) {
        UUID ownerId = extractUserIdFromPrincipal(principal);
        try {
            PublishWorkflowResponse response = workflowPublishService.publishWorkflow(ownerId, workflowId, request.definitionJson());
            return ResponseEntity.ok(response);
        } catch (WorkflowPublishService.WorkflowNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (WorkflowPublishService.WorkflowValidationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/{workflowId}/activate")
    @Operation(summary = "Activate workflow")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<WorkflowResponse> activateWorkflow(
            Principal principal,
            @PathVariable UUID workflowId
    ) {
        UUID ownerId = extractUserIdFromPrincipal(principal);
        try {
            return ResponseEntity.ok(workflowActivationService.activateWorkflow(ownerId, workflowId));
        } catch (WorkflowActivationService.WorkflowNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (WorkflowActivationService.InvalidWorkflowStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    @PostMapping("/{workflowId}/deactivate")
    @Operation(summary = "Deactivate workflow")
    @SecurityRequirement(name = "bearer")
    public ResponseEntity<WorkflowResponse> deactivateWorkflow(
            Principal principal,
            @PathVariable UUID workflowId
    ) {
        UUID ownerId = extractUserIdFromPrincipal(principal);
        try {
            return ResponseEntity.ok(workflowActivationService.deactivateWorkflow(ownerId, workflowId));
        } catch (WorkflowActivationService.WorkflowNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (WorkflowActivationService.InvalidWorkflowStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }

    private UUID extractUserIdFromPrincipal(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        String principalStr = principal.getName();

        try {
            return UUID.fromString(principalStr);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(principalStr.getBytes());
        }
    }
}




