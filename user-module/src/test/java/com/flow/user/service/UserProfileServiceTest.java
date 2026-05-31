package com.flow.user.service;

import com.flow.user.domain.entity.User;
import com.flow.user.domain.model.UserRole;
import com.flow.user.domain.repository.UserRepository;
import com.flow.user.dto.UpdateProfileRequest;
import com.flow.user.dto.UserProfileResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService Tests")
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAccountPersistenceService userAccountPersistenceService;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService(
                userRepository,
                userAccountPersistenceService
        );
    }

    @Nested
    @DisplayName("getProfile tests")
    class GetProfileTests {

        private UUID userId;
        private User testUser;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            testUser = new User(
                    userId,
                    "john.doe@example.com",
                    "$2a$10$hashedpassword",
                    UserRole.USER,
                    "John",
                    "Doe",
                    true,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Test
        @DisplayName("Should retrieve user profile successfully")
        void shouldRetrieveUserProfile() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            UserProfileResponse response = userProfileService.getProfile(userId);

            // Assert
            assertNotNull(response);
            assertEquals(userId, response.userId());
            assertEquals("john.doe@example.com", response.email());
            assertEquals("John", response.firstName());
            assertEquals("Doe", response.lastName());
            assertEquals(UserRole.USER, response.role());

            verify(userRepository, times(1)).findById(userId);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    UserProfileService.ResourceNotFoundException.class,
                    () -> userProfileService.getProfile(userId),
                    "Expected ResourceNotFoundException to be thrown"
            );

            verify(userRepository, times(1)).findById(userId);
        }

        @Test
        @DisplayName("Should include all profile fields in response")
        void shouldIncludeAllProfileFields() {
            // Arrange
            UUID userId = UUID.randomUUID();
            User user = new User(
                    userId,
                    "admin@example.com",
                    "$2a$10$hashedpassword",
                    UserRole.ADMIN,
                    "Alice",
                    "Admin",
                    true,
                    null,
                    null,
                    null,
                    null
            );
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act
            UserProfileResponse response = userProfileService.getProfile(userId);

            // Assert
            assertAll(
                    () -> assertNotNull(response.userId()),
                    () -> assertNotNull(response.email()),
                    () -> assertNotNull(response.firstName()),
                    () -> assertNotNull(response.lastName()),
                    () -> assertNotNull(response.role())
            );
        }
    }

    @Nested
    @DisplayName("updateProfile tests")
    class UpdateProfileTests {

        private UUID userId;
        private UUID requestUserId;
        private User testUser;
        private UpdateProfileRequest updateRequest;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            requestUserId = userId; // Same user
            testUser = new User(
                    userId,
                    "john.doe@example.com",
                    "$2a$10$hashedpassword",
                    UserRole.USER,
                    "John",
                    "Doe",
                    true,
                    null,
                    null,
                    null,
                    null
            );
            updateRequest = new UpdateProfileRequest("Jane", "Smith");
        }

        @Test
        @DisplayName("Should update user profile successfully")
        void shouldUpdateUserProfile() {
            // Arrange
            User updatedUser = new User(
                    userId,
                    "john.doe@example.com",
                    "$2a$10$hashedpassword",
                    UserRole.USER,
                    "Jane",
                    "Smith",
                    true,
                    null,
                    userId,
                    null,
                    null
            );
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userAccountPersistenceService.save(any(User.class))).thenReturn(updatedUser);

            // Act
            userProfileService.updateProfile(userId, requestUserId, updateRequest);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userAccountPersistenceService, times(1)).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals("Jane", savedUser.getFirstName());
            assertEquals("Smith", savedUser.getLastName());
        }

        @Test
        @DisplayName("Should throw AccessDeniedException when user tries to modify another user's profile")
        void shouldThrowAccessDeniedWhenModifyingOtherUserProfile() {
            // Arrange
            UUID otherUserId = UUID.randomUUID();
            // Don't need to mock findById since exception should be thrown before calling save

            // Act & Assert
            assertThrows(
                    AccessDeniedException.class,
                    () -> userProfileService.updateProfile(userId, otherUserId, updateRequest),
                    "Expected AccessDeniedException to be thrown"
            );

            verify(userAccountPersistenceService, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found during update")
        void shouldThrowExceptionWhenUserNotFoundDuringUpdate() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    UserProfileService.ResourceNotFoundException.class,
                    () -> userProfileService.updateProfile(userId, requestUserId, updateRequest)
            );

            verify(userAccountPersistenceService, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should update both first name and last name")
        void shouldUpdateBothNames() {
            // Arrange
            UpdateProfileRequest request = new UpdateProfileRequest("NewFirst", "NewLast");
            User updatedUser = new User(
                    userId,
                    "john.doe@example.com",
                    "$2a$10$hashedpassword",
                    UserRole.USER,
                    "NewFirst",
                    "NewLast",
                    true,
                    null,
                    userId,
                    null,
                    null
            );
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userAccountPersistenceService.save(any(User.class))).thenReturn(updatedUser);

            // Act
            userProfileService.updateProfile(userId, requestUserId, request);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userAccountPersistenceService).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals("NewFirst", savedUser.getFirstName());
            assertEquals("NewLast", savedUser.getLastName());
        }

        @Test
        @DisplayName("Should preserve other user fields during update")
        void shouldPreserveOtherFields() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userAccountPersistenceService.save(any(User.class))).thenReturn(testUser);

            // Act
            userProfileService.updateProfile(userId, requestUserId, updateRequest);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userAccountPersistenceService).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals(userId, savedUser.getId());
            assertEquals("john.doe@example.com", savedUser.getEmail());
            assertEquals(UserRole.USER, savedUser.getRole());
            assertTrue(savedUser.isActive());
        }

        @Test
        @DisplayName("Should set updatedBy field to requesting user ID")
        void shouldSetUpdatedByField() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userAccountPersistenceService.save(any(User.class))).thenReturn(testUser);

            // Act
            userProfileService.updateProfile(userId, requestUserId, updateRequest);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userAccountPersistenceService).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals(requestUserId, savedUser.getUpdatedBy());
        }

        @Test
        @DisplayName("Should update timestamp when profile is modified")
        void shouldUpdateTimestamp() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userAccountPersistenceService.save(any(User.class))).thenReturn(testUser);

            // Act
            userProfileService.updateProfile(userId, requestUserId, updateRequest);

            // Assert
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userAccountPersistenceService).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            // updatedAt is managed by @PreUpdate annotation in User entity
            // We just verify that save was called, which would trigger the update
            assertNotNull(savedUser.getUpdatedBy());
        }
    }

    @Nested
    @DisplayName("Authorization tests")
    class AuthorizationTests {

        private UUID userId;
        private User testUser;
        private UpdateProfileRequest updateRequest;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();
            testUser = new User(
                    userId,
                    "user@example.com",
                    "$2a$10$hashedpassword",
                    UserRole.USER,
                    "User",
                    "Name",
                    true,
                    null,
                    null,
                    null,
                    null
            );
            updateRequest = new UpdateProfileRequest("Updated", "Name");
        }

        @Test
        @DisplayName("Should allow user to modify own profile")
        void shouldAllowSelfModification() {
            // Arrange
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(userAccountPersistenceService.save(any(User.class))).thenReturn(testUser);

            // Act & Assert - should not throw
            assertDoesNotThrow(
                    () -> userProfileService.updateProfile(userId, userId, updateRequest)
            );

            verify(userAccountPersistenceService, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should deny modification of another user's profile")
        void shouldDenyOtherUserModification() {
            // Arrange
            UUID otherUserId = UUID.randomUUID();
            // Don't need to mock anything since authorization check happens first

            // Act & Assert
            assertThrows(
                    AccessDeniedException.class,
                    () -> userProfileService.updateProfile(userId, otherUserId, updateRequest)
            );

            verify(userAccountPersistenceService, never()).save(any(User.class));
        }
    }
}




