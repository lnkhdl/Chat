package com.lnkhdl.chat.common.name;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameValidatorTest {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 30;

    @Test
    void testSanitizeName() {
        String input = "  TestUser  ";
        String sanitized = NameValidator.sanitize(input);
        assertEquals("testuser", sanitized);
    }

    @Test
    void testSanitizeEmptyName() {
        String input = "";
        String sanitized = NameValidator.sanitize(input);
        assertNull(sanitized);
    }

    @Test
    void testValidateNameValid() {
        String validName = "testuser";
        NameValidationResult result = NameValidator.validate(NameValidator.sanitize(validName));
        assertTrue(result.isValid());
    }

    @Test
    void testValidateNameTooShort() {
        String tooShortName = "ab";
        NameValidationResult result = NameValidator.validate(NameValidator.sanitize(tooShortName));
        assertFalse(result.isValid());
        assertEquals("Username must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.", result.getErrorMessage());
    }

    @Test
    void testValidateNameTooLong() {
        String tooLongName = "thisisaveeeeeeeeerylongusername";
        NameValidationResult result = NameValidator.validate(NameValidator.sanitize(tooLongName));
        assertFalse(result.isValid());
        assertEquals("Username must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.", result.getErrorMessage());
    }

    @Test
    void testValidateNameInvalidCharacters() {
        String invalidName = "test@user!";
        NameValidationResult result = NameValidator.validate(NameValidator.sanitize(invalidName));
        assertFalse(result.isValid());
        assertEquals("Username can only contain letters, numbers, and underscores.", result.getErrorMessage());
    }

    @Test
    void testValidateNameEmpty() {
        String emptyName = "";
        NameValidationResult result = NameValidator.validate(NameValidator.sanitize(emptyName));
        assertFalse(result.isValid());
        assertEquals("Username cannot be empty. Please try another one.", result.getErrorMessage());
    }
}