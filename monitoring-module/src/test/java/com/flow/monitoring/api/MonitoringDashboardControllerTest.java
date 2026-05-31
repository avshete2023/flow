package com.flow.monitoring.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flow.monitoring.dto.DashboardResponse;
import com.flow.monitoring.dto.ExecutionTrendPoint;
import com.flow.monitoring.service.MonitoringDashboardService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MonitoringDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class MonitoringDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitoringDashboardService monitoringDashboardService;

    @Test
    void shouldReturnDashboardPayload() throws Exception {
        DashboardResponse response = new DashboardResponse(
                10,
                7,
                2,
                1,
                0,
                List.of(new ExecutionTrendPoint(LocalDate.now(), 10)),
                Map.of("total", 10L)
        );
        when(monitoringDashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/v1/monitoring/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExecutions").value(10))
                .andExpect(jsonPath("$.successfulExecutions").value(7))
                .andExpect(jsonPath("$.failedExecutions").value(2));
    }
}

