package com.flow.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flow.user.domain.entity.User;
import com.flow.user.domain.model.UserRole;
import com.flow.user.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAccountPersistenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAccountPersistenceService userAccountPersistenceService;

    @Test
    void shouldDelegateFindByEmailToRepository() {
        User user = new User(
                UUID.randomUUID(),
                "john@example.com",
                "$2a$12$abcdefghijklmnopqrstuv123456789012345678901234567890",
                UserRole.USER,
                "John",
                "Doe",
                true,
                null,
                null,
                null,
                null
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userAccountPersistenceService.findByEmail("john@example.com");

        assertThat(result).contains(user);
        verify(userRepository).findByEmail("john@example.com");
    }
}

