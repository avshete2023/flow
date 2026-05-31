package com.flow.user.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.flow.user.domain.entity.User;
import com.flow.user.domain.model.UserRole;
import com.flow.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistUserWithAuditFields() {
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

        User saved = userRepository.saveAndFlush(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getPasswordHash()).doesNotContain("Password@123");
        assertThat(userRepository.findByEmail("john@example.com")).isPresent();
    }

    @Test
    void shouldRejectDuplicateEmail() {
        userRepository.saveAndFlush(new User(
                UUID.randomUUID(),
                "duplicate@example.com",
                "$2a$12$abcdefghijklmnopqrstuv123456789012345678901234567890",
                UserRole.USER,
                "Jane",
                "Doe",
                true,
                null,
                null,
                null,
                null
        ));

        User duplicate = new User(
                UUID.randomUUID(),
                "duplicate@example.com",
                "$2a$12$zzzzzzzzzzzzzzzzzzzzzz123456789012345678901234567890",
                UserRole.ADMIN,
                "Janet",
                "Smith",
                true,
                null,
                null,
                null,
                null
        );

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(duplicate));
    }

    @Test
    void shouldRequireMandatoryFields() {
        User invalidUser = new User(
                UUID.randomUUID(),
                null,
                null,
                null,
                "John",
                "Doe",
                true,
                null,
                null,
                null,
                null
        );

        assertThrows(ConstraintViolationException.class, () -> {
            userRepository.save(invalidUser);
            userRepository.flush();
            entityManager.clear();
        });
    }
}


