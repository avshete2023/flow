package com.flow.user.dto;

import com.flow.user.domain.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("UserProfileResponse DTO Tests")
class UserProfileResponseTest {

    @Test
    @DisplayName("Should create valid UserProfileResponse with all fields")
    void testCreateValidResponse() {
        UUID userId = UUID.randomUUID();
        String email = "john@example.com";
        String firstName = "John";
        String lastName = "Doe";
        UserRole role = UserRole.USER;

        UserProfileResponse response = new UserProfileResponse(
                userId,
                email,
                firstName,
                lastName,
                role
        );

        assertNotNull(response, "Response should not be null");
        assertEquals(userId, response.userId(), "User ID should match");
        assertEquals(email, response.email(), "Email should match");
        assertEquals(firstName, response.firstName(), "First name should match");
        assertEquals(lastName, response.lastName(), "Last name should match");
        assertEquals(role, response.role(), "Role should match");
    }

    @Test
    @DisplayName("Should preserve all data in record")
    void testRecordFieldPreservation() {
        UUID userId = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        String email = "admin@example.com";
        String firstName = "Jane";
        String lastName = "Smith";
        UserRole role = UserRole.ADMIN;

        UserProfileResponse response = new UserProfileResponse(
                userId,
                email,
                firstName,
                lastName,
                role
        );

        // Verify all fields are preserved
        assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479", response.userId().toString());
        assertEquals("admin@example.com", response.email());
        assertEquals("Jane", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals(UserRole.ADMIN, response.role());
    }

    @Test
    @DisplayName("Should support record equality")
    void testRecordEquality() {
        UUID userId = UUID.randomUUID();
        UserProfileResponse response1 = new UserProfileResponse(
                userId,
                "user@example.com",
                "John",
                "Doe",
                UserRole.USER
        );
        UserProfileResponse response2 = new UserProfileResponse(
                userId,
                "user@example.com",
                "John",
                "Doe",
                UserRole.USER
        );

        assertEquals(response1, response2, "Responses with same data should be equal");
    }

    @Test
    @DisplayName("Should work with different role types")
    void testMultipleRoles() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";

        for (UserRole role : UserRole.values()) {
            UserProfileResponse response = new UserProfileResponse(
                    userId,
                    email,
                    firstName,
                    lastName,
                    role
            );
            assertEquals(role, response.role(), "Role should be " + role);
        }
    }
}

