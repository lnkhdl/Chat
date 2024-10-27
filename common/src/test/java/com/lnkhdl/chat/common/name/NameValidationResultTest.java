package com.lnkhdl.chat.common.name;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameValidationResultTest {

    @Test
    void testCreateValidResult() {
        NameValidationResult result = NameValidationResult.createValidResult();
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    void createInvalidResult() {
        String errorMessage = "Invalid username.";
        NameValidationResult result = NameValidationResult.createInvalidResult(errorMessage);
        assertFalse(result.isValid());
        assertEquals(errorMessage, result.getErrorMessage());
    }
}