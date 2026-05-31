package com.flow.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

@SpringBootTest
class FlowApiApplicationTests {

    private static final String JWT_SECRET = UUID.randomUUID().toString().replace("-", "")
            + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void registerJwtProperties(DynamicPropertyRegistry registry) {
        registry.add("flow.security.jwt.secret", () -> JWT_SECRET);
    }

    @Test
    void contextLoads() {
    }
}
