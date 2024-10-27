package com.lnkhdl.chat.common.exception;

public class InvalidProtocolException extends Exception {
    public InvalidProtocolException(String message) {
        super(message);
    }

    // The "cause" represents another throwable (exception or error) that caused this exception to be thrown.
    public InvalidProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}