package com.flow.user.service;

import com.flow.user.domain.entity.User;
import com.flow.user.domain.repository.UserRepository;
import com.flow.user.dto.UpdateProfileRequest;
import com.flow.user.dto.UserProfileResponse;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user profile operations.
 *
 * Responsibilities:
 * - Retrieve user profile
 * - Update user profile
 * - Enforce authorization constraints
 *
 * Security:
 * - Users can only access and modify their own profile
 */
@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserAccountPersistenceService userAccountPersistenceService;

    public UserProfileService(
            UserRepository userRepository,
            UserAccountPersistenceService userAccountPersistenceService
    ) {
        this.userRepository = userRepository;
        this.userAccountPersistenceService = userAccountPersistenceService;
    }

    /**
     * Retrieve the profile of a user.
     *
     * @param userId The UUID of the user
     * @return UserProfileResponse containing user profile information
     * @throws ResourceNotFoundException if user does not exist
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    /**
     * Update the profile of a user.
     *
     * Authorization:
     * - Users can only update their own profile
     *
     * @param userId The UUID of the user to update (from security context)
     * @param requestUserId The UUID of the user making the request
     * @param updateRequest Contains updated first name and last name
     * @throws AccessDeniedException if user attempts to modify another user's profile
     * @throws ResourceNotFoundException if user does not exist
     */
    @Transactional
    public void updateProfile(UUID userId, UUID requestUserId, UpdateProfileRequest updateRequest) {
        // Authorization check: users can only modify their own profile
        if (!userId.equals(requestUserId)) {
            throw new AccessDeniedException(
                    "Users can only modify their own profile"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        // Update profile fields
        user.setFirstName(updateRequest.firstName());
        user.setLastName(updateRequest.lastName());
        user.setUpdatedBy(requestUserId);
        // Note: updatedAt is automatically set by @PreUpdate annotation in User entity

        // Persist changes
        userAccountPersistenceService.save(user);
    }

    /**
     * Custom exception for resource not found scenarios.
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}



