package com.flow.workflow.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.dto.CreateWorkflowRequest;
import com.flow.workflow.dto.PublishWorkflowRequest;
import com.flow.workflow.dto.PublishWorkflowResponse;
import com.flow.workflow.dto.UpdateWorkflowRequest;
import com.flow.workflow.dto.WorkflowResponse;
import com.flow.workflow.service.WorkflowActivationService;
import com.flow.workflow.service.WorkflowCrudService;
import com.flow.workflow.service.WorkflowPublishService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkflowController.class)
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkflowCrudService workflowCrudService;

    @MockBean
    private WorkflowPublishService workflowPublishService;

    @MockBean
    private WorkflowActivationService workflowActivationService;

    private UUID userId;
    private UUID workflowId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        workflowId = UUID.randomUUID();
    }

    @Test
    void shouldRequireAuthenticationForWorkflowEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/workflows/{workflowId}", workflowId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateWorkflow() throws Exception {
        WorkflowResponse response = new WorkflowResponse(workflowId, "Order", "desc", WorkflowStatus.DRAFT, null);
        when(workflowCrudService.createWorkflow(any(UUID.class), any(CreateWorkflowRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/workflows")
                        .with(user(userId.toString()).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWorkflowRequest("Order", "desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workflowId").value(workflowId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void shouldRejectInvalidCreateWorkflowRequest() throws Exception {
        mockMvc.perform(post("/api/v1/workflows")
                        .with(user(userId.toString()).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWorkflowRequest("", "desc"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetWorkflow() throws Exception {
        WorkflowResponse response = new WorkflowResponse(workflowId, "Order", "desc", WorkflowStatus.DRAFT, null);
        when(workflowCrudService.getWorkflow(any(UUID.class), eq(workflowId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/workflows/{workflowId}", workflowId)
                        .with(user(userId.toString()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowId").value(workflowId.toString()));
    }

    @Test
    void shouldUpdateWorkflow() throws Exception {
        WorkflowResponse response = new WorkflowResponse(workflowId, "Updated", "updated", WorkflowStatus.DRAFT, null);
        when(workflowCrudService.updateWorkflow(any(UUID.class), eq(workflowId), any(UpdateWorkflowRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/workflows/{workflowId}", workflowId)
                        .with(user(userId.toString()).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateWorkflowRequest("Updated", "updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldDeleteWorkflow() throws Exception {
        doNothing().when(workflowCrudService).deleteWorkflow(any(UUID.class), eq(workflowId));

        mockMvc.perform(delete("/api/v1/workflows/{workflowId}", workflowId)
                        .with(user(userId.toString()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldListWorkflowsWithPagination() throws Exception {
        WorkflowResponse item = new WorkflowResponse(workflowId, "Order", "desc", WorkflowStatus.DRAFT, null);
        when(workflowCrudService.listWorkflows(any(UUID.class), eq(WorkflowStatus.DRAFT), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/workflows")
                        .with(user(userId.toString()).roles("USER"))
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].workflowId").value(workflowId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldPublishWorkflow() throws Exception {
        PublishWorkflowResponse response = new PublishWorkflowResponse(UUID.randomUUID(), 2, WorkflowStatus.PUBLISHED);
        when(workflowPublishService.publishWorkflow(any(UUID.class), eq(workflowId), any(String.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/publish", workflowId)
                        .with(user(userId.toString()).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PublishWorkflowRequest("{\"trigger\":{}}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void shouldActivateAndDeactivateWorkflow() throws Exception {
        WorkflowResponse active = new WorkflowResponse(workflowId, "Order", "desc", WorkflowStatus.ACTIVE, UUID.randomUUID());
        WorkflowResponse inactive = new WorkflowResponse(workflowId, "Order", "desc", WorkflowStatus.INACTIVE, UUID.randomUUID());
        when(workflowActivationService.activateWorkflow(any(UUID.class), eq(workflowId))).thenReturn(active);
        when(workflowActivationService.deactivateWorkflow(any(UUID.class), eq(workflowId))).thenReturn(inactive);

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/activate", workflowId)
                        .with(user(userId.toString()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/workflows/{workflowId}/deactivate", workflowId)
                        .with(user(userId.toString()).roles("USER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}


