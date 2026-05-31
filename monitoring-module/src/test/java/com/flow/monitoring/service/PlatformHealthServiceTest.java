package com.flow.monitoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;

class PlatformHealthServiceTest {

    @Test
    void shouldReturnDependencyStatuses() {
        HealthEndpoint healthEndpoint = org.mockito.Mockito.mock(HealthEndpoint.class);
        when(healthEndpoint.healthForPath("db")).thenReturn(Health.up().build());
        when(healthEndpoint.healthForPath("rabbit")).thenReturn(Health.down().build());
        when(healthEndpoint.healthForPath("redis")).thenReturn(Health.unknown().build());

        PlatformHealthService platformHealthService = new PlatformHealthService(healthEndpoint);

        Map<String, String> status = platformHealthService.dependencyHealth();

        assertThat(status).containsEntry("postgresql", "UP");
        assertThat(status).containsEntry("rabbitmq", "DOWN");
        assertThat(status).containsEntry("redis", "UNKNOWN");
    }
}

