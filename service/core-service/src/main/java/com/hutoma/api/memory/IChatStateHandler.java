package com.hutoma.api.memory;

import com.hutoma.api.containers.sub.ChatState;

import java.util.UUID;

public interface IChatStateHandler {
    ChatState getState(UUID devId, UUID aiid, UUID chatId) throws ChatStateException;

    void saveState(UUID devId, UUID aiid, UUID chatId, ChatState chatState)
            throws ChatStateException;

    void clear(UUID devId, UUID aiid, UUID chatId, ChatState chatState)
                    throws ChatStateException;
}
