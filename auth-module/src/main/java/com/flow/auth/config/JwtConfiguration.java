package com.flow.auth.config;

import com.flow.auth.security.JwtTokenGenerator;
import com.flow.auth.security.JwtTokenValidator;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    @Bean
    public Clock jwtClock() {
        return Clock.systemUTC();
    }

    @Bean
    public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JwtTokenGenerator jwtTokenGenerator(SecretKey jwtSecretKey, Clock jwtClock, JwtProperties jwtProperties) {
        return new JwtTokenGenerator(jwtSecretKey, jwtClock, jwtProperties);
    }

    @Bean
    public JwtTokenValidator jwtTokenValidator(SecretKey jwtSecretKey, JwtProperties jwtProperties) {
        return new JwtTokenValidator(jwtSecretKey, jwtProperties);
    }
}

