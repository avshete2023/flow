package com.flow.audit.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flow.audit.dto.AuditEventResponse;
import com.flow.audit.service.AuditService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    void shouldReturnPaginatedAuditEventsWithFilters() throws Exception {
        UUID auditId = UUID.randomUUID();
        AuditEventResponse item = new AuditEventResponse(
                auditId,
                UUID.randomUUID(),
                "LOGIN",
                "SECURITY",
                null,
                "{}",
                LocalDateTime.now()
        );

        when(auditService.listAuditEvents(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/audit")
                        .param("page", "0")
                        .param("size", "20")
                        .param("eventType", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].auditId").value(auditId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnAuditEventById() throws Exception {
        UUID auditId = UUID.randomUUID();
        when(auditService.getAuditEvent(auditId)).thenReturn(new AuditEventResponse(
                auditId,
                UUID.randomUUID(),
                "WORKFLOW_PUBLISHED",
                "WORKFLOW",
                UUID.randomUUID(),
                "{}",
                LocalDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/audit/{auditId}", auditId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auditId").value(auditId.toString()))
                .andExpect(jsonPath("$.eventType").value("WORKFLOW_PUBLISHED"));
    }

    @Test
    void shouldRejectUnsupportedSortField() throws Exception {
        mockMvc.perform(get("/api/v1/audit").param("sort", "unknown,desc"))
                .andExpect(status().isBadRequest());
    }
}

