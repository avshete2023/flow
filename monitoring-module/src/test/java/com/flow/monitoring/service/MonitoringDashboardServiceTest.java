package com.flow.monitoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.flow.execution.service.ExecutionMonitoringQueryService;
import com.flow.execution.service.ExecutionStatistics;
import com.flow.monitoring.dto.DashboardResponse;
import org.junit.jupiter.api.Test;

class MonitoringDashboardServiceTest {

    @Test
    void shouldBuildDashboardFromExecutionStatistics() {
        ExecutionMonitoringQueryService queryService = org.mockito.Mockito.mock(ExecutionMonitoringQueryService.class);
        when(queryService.getExecutionStatistics()).thenReturn(new ExecutionStatistics(25, 20, 3, 1, 1, 4));

        MonitoringDashboardService service = new MonitoringDashboardService(queryService);

        DashboardResponse response = service.getDashboard();

        assertThat(response.totalExecutions()).isEqualTo(25);
        assertThat(response.successfulExecutions()).isEqualTo(20);
        assertThat(response.failedExecutions()).isEqualTo(3);
        assertThat(response.runningExecutions()).isEqualTo(1);
        assertThat(response.dlqExecutions()).isEqualTo(1);
        assertThat(response.executionTrends()).hasSize(1);
        assertThat(response.workflowStatistics()).containsEntry("total", 25L);
    }
}

