package com.lnkhdl.chat.common.message;

// Message types that the server-client can exchange
public enum Type {
    INIT,
    SET_USERNAME,
    CONFIRM_USERNAME,
    WRONG_USERNAME,
    MESSAGE,
    PRIVATE_MESSAGE,
    WRONG_PRIVATE_MESSAGE_USERNAME,
    WRONG_MESSAGE_FORMAT,
    SERVER_CLOSED
}