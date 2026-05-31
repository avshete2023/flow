package com.flow.workflow.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.workflow.domain.model.WorkflowStatus;
import com.flow.workflow.dto.CreateWorkflowRequest;
import com.flow.workflow.dto.WorkflowResponse;
import com.flow.workflow.service.WorkflowActivationService;
import com.flow.workflow.service.WorkflowCrudService;
import com.flow.workflow.service.WorkflowPublishService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowControllerIntegrationTest {

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

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/workflows/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateWorkflowWithAuthenticatedUser() throws Exception {
        UUID workflowId = UUID.randomUUID();
        WorkflowResponse response = new WorkflowResponse(workflowId, "Order", "desc", WorkflowStatus.DRAFT, null);
        when(workflowCrudService.createWorkflow(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/workflows")
                        .with(user(UUID.randomUUID().toString()).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWorkflowRequest("Order", "desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workflowId").value(workflowId.toString()));
    }
}

