package com.lnkhdl.chat.common.name;

// At the beginning of a communication, each client has to set its username. The username is then used as a clientName on the server.
public class NameValidator {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 30;

    public static String sanitize(String name) {
        if (name.isBlank()) {
            return null;
        }
        return name.trim().toLowerCase();
    }

    public static NameValidationResult validate(String name) {
        if (name == null) {
            return NameValidationResult.createInvalidResult("Username cannot be empty. Please try another one.");
        }

        int length = name.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            return NameValidationResult.createInvalidResult("Username must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.");
        }

        // Validate the username using a regex (e.g., only alphanumeric and underscores)
        if (!name.matches("^[a-z0-9_]+$")) {
            return NameValidationResult.createInvalidResult("Username can only contain letters, numbers, and underscores.");
        }

        return NameValidationResult.createValidResult();
    }
}