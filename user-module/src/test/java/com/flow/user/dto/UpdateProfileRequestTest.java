package com.flow.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("UpdateProfileRequest DTO Tests")
class UpdateProfileRequestTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid UpdateProfileRequest with valid data")
    void testValidUpdateProfileRequest() {
        UpdateProfileRequest request = new UpdateProfileRequest("John", "Doe");
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    @DisplayName("Should reject null first name")
    void testNullFirstName() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, "Doe");
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should have 1 violation for null firstName");
    }

    @Test
    @DisplayName("Should reject blank first name")
    void testBlankFirstName() {
        UpdateProfileRequest request = new UpdateProfileRequest("", "Doe");
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size(), "Should have 2 violations for blank firstName (@NotBlank and @NotNull)");
    }

    @Test
    @DisplayName("Should reject white space only first name")
    void testWhitespaceFirstName() {
        UpdateProfileRequest request = new UpdateProfileRequest("   ", "Doe");
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should have 1 violation for whitespace-only firstName");
    }

    @Test
    @DisplayName("Should reject first name exceeding max length")
    void testFirstNameTooLong() {
        String longName = "a".repeat(101);
        UpdateProfileRequest request = new UpdateProfileRequest(longName, "Doe");
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should have 1 violation for firstName exceeding 100 chars");
    }

    @Test
    @DisplayName("Should accept first name of exactly 100 characters")
    void testFirstNameMaxLength() {
        String maxName = "a".repeat(100);
        UpdateProfileRequest request = new UpdateProfileRequest(maxName, "Doe");
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should accept firstName of exactly 100 characters");
    }

    @Test
    @DisplayName("Should reject null last name")
    void testNullLastName() {
        UpdateProfileRequest request = new UpdateProfileRequest("John", null);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should have 1 violation for null lastName");
    }

    @Test
    @DisplayName("Should reject blank last name")
    void testBlankLastName() {
        UpdateProfileRequest request = new UpdateProfileRequest("John", "");
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size(), "Should have 2 violations for blank lastName (@NotBlank and @NotNull)");
    }

    @Test
    @DisplayName("Should reject last name exceeding max length")
    void testLastNameTooLong() {
        String longName = "b".repeat(101);
        UpdateProfileRequest request = new UpdateProfileRequest("John", longName);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size(), "Should have 1 violation for lastName exceeding 100 chars");
    }

    @ParameterizedTest
    @ValueSource(strings = {"J", "Jo", "John", "Jean-Baptiste", "Mary-Ann"})
    @DisplayName("Should accept valid first names of various lengths")
    void testVariousValidFirstNames(String firstName) {
        UpdateProfileRequest request = new UpdateProfileRequest(firstName, "Doe");
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid first name should be accepted: " + firstName);
    }

    @ParameterizedTest
    @ValueSource(strings = {"S", "So", "Smith", "Van-der-Waals", "O'Brien"})
    @DisplayName("Should accept valid last names of various lengths")
    void testVariousValidLastNames(String lastName) {
        UpdateProfileRequest request = new UpdateProfileRequest("John", lastName);
        Set<ConstraintViolation<UpdateProfileRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid last name should be accepted: " + lastName);
    }
}



