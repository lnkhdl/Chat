package com.lnkhdl.chat.common.message;

import com.lnkhdl.chat.common.exception.InvalidProtocolException;

public class Protocol {
    public static final String DELIMITER = "::";
    public static final String PRIVATE_MESSAGE_MARK = "/w";

    public static String formatMessage(Type type, String content) {
        return type + DELIMITER + content;
    }

    public static Type parseMessageType(String message) throws InvalidProtocolException {
        String[] parts = parseMessage(message);
        try {
            return Type.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new InvalidProtocolException("Invalid message type: " + parts[0], e);
        }
    }

    public static String parseMessageText(String message) throws InvalidProtocolException {
        String[] parts = parseMessage(message);
        return parts[1];
    }

    private static String[] parseMessage(String message) throws InvalidProtocolException {
        String[] parts = message.split(DELIMITER, 2);
        if (parts.length < 2) {
            throw new InvalidProtocolException("Malformed message, missing type or content: " + message);
        }
        return parts;
    }

    public static String[] parsePrivateMessageText(String message) throws InvalidProtocolException {
        // Split the message into 3 parts: /w, targetUsername, and the message
        String[] parts = message.split(" ", 3);

        if (!parts[0].equals(PRIVATE_MESSAGE_MARK) || parts.length < 3) {
            throw new InvalidProtocolException("The private message text is not correctly formatted: " + message);
        }
        return parts;
    }
}