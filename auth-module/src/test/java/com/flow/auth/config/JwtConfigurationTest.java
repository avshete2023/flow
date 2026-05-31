package com.flow.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.flow.auth.security.JwtTokenGenerator;
import com.flow.auth.security.JwtTokenValidator;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import javax.crypto.SecretKey;

class JwtConfigurationTest {

    private static final String JWT_SECRET = "12345678901234567890123456789012";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JwtConfiguration.class)
            .withPropertyValues(
                    "flow.security.jwt.secret=" + JWT_SECRET,
                    "flow.security.jwt.issuer=flow-api",
                    "flow.security.jwt.access-token-ttl=PT60M"
            );

    @Test
    void shouldCreateJwtBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JwtProperties.class);
            assertThat(context).hasSingleBean(SecretKey.class);
            assertThat(context).hasSingleBean(JwtTokenGenerator.class);
            assertThat(context).hasSingleBean(JwtTokenValidator.class);
        });
    }

    @Test
    void shouldBindJwtProperties() {
        contextRunner.run(context -> {
            JwtProperties properties = context.getBean(JwtProperties.class);

            assertThat(properties.getSecret()).isEqualTo(JWT_SECRET);
            assertThat(properties.getIssuer()).isEqualTo("flow-api");
            assertThat(properties.getAccessTokenTtl()).isEqualTo(Duration.ofMinutes(60));
        });
    }
}

