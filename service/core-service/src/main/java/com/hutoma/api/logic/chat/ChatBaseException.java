package com.hutoma.api.logic.chat;

public abstract class ChatBaseException extends Exception {

    public ChatBaseException() {
    }

    public ChatBaseException(final String message) {
        super(message);
    }

    public ChatBaseException(final Throwable cause) {
        super(cause);
    }

    protected ChatBaseException(String message, Throwable e) {
        super(message, e);
    }
}
