package com.flow.auth.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flow.auth.api.AuthController;
import com.flow.auth.config.JwtConfiguration;
import com.flow.auth.config.SecurityConfiguration;
import com.flow.auth.dto.LoginResponse;
import com.flow.auth.service.AuthenticationService;
import com.flow.user.domain.model.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AuthController.class})
@Import({
        SecurityConfiguration.class,
        JwtConfiguration.class,
        JwtAuthenticationFilter.class
})
@TestPropertySource(properties = {
        "flow.security.jwt.secret=12345678901234567890123456789012",
        "flow.security.jwt.issuer=flow-api",
        "flow.security.jwt.access-token-ttl=PT60M"
})
class SecurityConfigurationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void shouldAllowPublicLoginEndpointWithoutToken() throws Exception {
        when(authenticationService.login(any())).thenReturn(new LoginResponse("access-token", "refresh-token", 3600));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "password": "Password@123"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/security-probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldPassSecurityForProtectedEndpointWithValidJwt() throws Exception {
        String token = jwtTokenGenerator.generateAccessToken(
                new JwtPrincipal(UUID.randomUUID(), "john@example.com", UserRole.USER)
        );

        mockMvc.perform(get("/api/v1/users/security-probe")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectProtectedEndpointWithInvalidJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users/security-probe")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

}






