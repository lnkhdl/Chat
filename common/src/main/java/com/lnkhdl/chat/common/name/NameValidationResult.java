package com.lnkhdl.chat.common.name;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
// Result Object Pattern
public class NameValidationResult {
    private boolean valid;
    private String errorMessage;

    // Static factory method for valid result
    public static NameValidationResult createValidResult() {
        return new NameValidationResult(true, null);
    }

    // Static factory method for invalid result
    public static NameValidationResult createInvalidResult(String errorMessage) {
        return new NameValidationResult(false, errorMessage);
    }
}