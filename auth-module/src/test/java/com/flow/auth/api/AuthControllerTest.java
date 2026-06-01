package com.flow.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flow.auth.dto.LoginResponse;
import com.flow.auth.dto.RegisterUserResponse;
import com.flow.auth.exception.EmailAlreadyRegisteredException;
import com.flow.auth.exception.InvalidCredentialsException;
import com.flow.auth.service.AuthenticationService;
import com.flow.auth.service.RegistrationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private RegistrationService registrationService;

    @Test
    void shouldExposeRegisterEndpointAndReturnCreated() throws Exception {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        when(registrationService.register(any()))
                .thenReturn(new RegisterUserResponse(userId, "john@example.com", "Registration successful"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "password": "Password@123",
                                  "firstName": "John",
                                  "lastName": "Doe"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldRejectDuplicateEmailRegistrationWithConflictStatus() throws Exception {
        doThrow(new EmailAlreadyRegisteredException()).when(registrationService).register(any());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "password": "Password@123",
                                  "firstName": "John",
                                  "lastName": "Doe"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidRegistrationRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "short",
                                  "firstName": "",
                                  "lastName": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExposeLoginEndpointAndReturnTokens() throws Exception {
        when(authenticationService.login(any())).thenReturn(new LoginResponse("access-token", "refresh-token", 3600));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "password": "Password@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void shouldRejectInvalidCredentialsWithUnauthorizedStatus() throws Exception {
        doThrow(new InvalidCredentialsException()).when(authenticationService).login(any());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "john@example.com",
                                  "password": "WrongPassword@123"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}





