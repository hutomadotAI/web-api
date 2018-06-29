package com.hutoma.api.memory;

import com.hutoma.api.logic.chat.ChatBaseException;

public class ChatStateException extends ChatBaseException {
    ChatStateException(final String message) {
        super(message);
    }

    public ChatStateException(final Throwable cause) {
        super(cause);
    }

    protected ChatStateException(String message, Throwable e) {
        super(message, e);
    }
}
