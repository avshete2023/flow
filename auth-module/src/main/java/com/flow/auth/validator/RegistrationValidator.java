package com.flow.auth.validator;

import org.springframework.stereotype.Component;

@Component
public class RegistrationValidator {

    public void validatePasswordStrength(String password) {
        if (password == null || !isStrong(password)) {
            throw new IllegalArgumentException("Password does not meet strength requirements");
        }
    }

    private boolean isStrong(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
