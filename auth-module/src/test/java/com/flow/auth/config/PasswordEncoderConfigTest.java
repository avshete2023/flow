package com.flow.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderConfigTest {

    @Test
    void shouldExposePasswordEncoderBean() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PasswordEncoderConfig.class)) {
            PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

            assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        }
    }

    @Test
    void shouldHashAndVerifyPasswords() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PasswordEncoderConfig.class)) {
            PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
            String rawPassword = "Password@123";

            String encodedPassword = passwordEncoder.encode(rawPassword);

            assertThat(encodedPassword).isNotBlank();
            assertThat(encodedPassword).isNotEqualTo(rawPassword);
            assertThat(encodedPassword).startsWith("$2");
            assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
            assertThat(passwordEncoder.matches("WrongPassword", encodedPassword)).isFalse();
        }
    }
}
