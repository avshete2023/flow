package com.flow.monitoring.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

/**
 * Extracts dependency health states for platform monitoring use-cases.
 */
@Service
public class PlatformHealthService {

    private final HealthEndpoint healthEndpoint;

    public PlatformHealthService(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    public Map<String, String> dependencyHealth() {
        Map<String, String> dependencies = new LinkedHashMap<>();

        HealthComponent postgres = healthEndpoint.healthForPath("db");
        HealthComponent rabbit = healthEndpoint.healthForPath("rabbit");
        HealthComponent redis = healthEndpoint.healthForPath("redis");

        dependencies.put("postgresql", toStatus(postgres));
        dependencies.put("rabbitmq", toStatus(rabbit));
        dependencies.put("redis", toStatus(redis));

        return dependencies;
    }

    private String toStatus(HealthComponent healthComponent) {
        if (healthComponent == null) {
            return Status.UNKNOWN.getCode();
        }
        Status status = healthComponent.getStatus();
        return status == null ? Status.UNKNOWN.getCode() : status.getCode();
    }
}

