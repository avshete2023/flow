package com.flow.execution.service;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ExecutionRetryPolicy.class)
public class ExecutionServiceConfiguration {
}

