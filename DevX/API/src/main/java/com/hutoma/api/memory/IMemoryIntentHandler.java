package com.hutoma.api.memory;

import com.hutoma.api.containers.sub.MemoryIntent;

import java.util.List;
import java.util.UUID;

/**
 * Created by pedrotei on 07/10/16.
 */
public interface IMemoryIntentHandler {
    /**
     * Parses the AI response in search for an intent.
     * @param aaid the AI ID
     * @param chatId the Chat ID
     * @param response the AI response
     * @return the MemoryIntent, or null if no intent was found
     */
    MemoryIntent parseAiResponseForIntent(String devid, UUID aaid, UUID chatId, String response);

    /**
     * Gets the current intents state for this chat
     * @param aiid the AI ID
     * @param chatId the Chat ID
     * @return the list of current intent states
     */
    List<MemoryIntent> getCurrentIntentsStateForChat(UUID aiid, UUID chatId);

    /**
     * Updates the status of the intent to storage.
     * @param intent the intent
     */
    void updateStatus(MemoryIntent intent);
}