package com.flow.monitoring.api;

import com.flow.monitoring.dto.DashboardResponse;
import com.flow.monitoring.service.MonitoringDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Monitoring dashboard API.
 */
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringDashboardController {

    private final MonitoringDashboardService monitoringDashboardService;

    public MonitoringDashboardController(MonitoringDashboardService monitoringDashboardService) {
        this.monitoringDashboardService = monitoringDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(monitoringDashboardService.getDashboard());
    }
}



