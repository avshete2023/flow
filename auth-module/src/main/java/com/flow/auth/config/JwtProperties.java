package com.flow.auth.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "flow.security.jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    @NotBlank
    private String issuer = "flow-api";

    @NotNull
    private Duration accessTokenTtl = Duration.of(60, ChronoUnit.MINUTES);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    @AssertTrue(message = "JWT secret must be at least 32 characters long")
    public boolean isSecretStrongEnough() {
        return secret != null && secret.length() >= 32;
    }

    @AssertTrue(message = "JWT access token TTL must be positive")
    public boolean isAccessTokenTtlPositive() {
        return accessTokenTtl != null && !accessTokenTtl.isZero() && !accessTokenTtl.isNegative();
    }
}

