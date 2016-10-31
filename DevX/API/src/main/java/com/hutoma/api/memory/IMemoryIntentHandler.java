package com.hutoma.api.memory;

import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.MemoryIntent;

import java.util.List;
import java.util.UUID;

/**
 * Created by pedrotei on 07/10/16.
 */
public interface IMemoryIntentHandler {
    /**
     * Parses the AI response in search for an intent.
     * @param aiid     the AI ID
     * @param chatId   the Chat ID
     * @param response the AI response
     * @return the MemoryIntent, or null if no intent was found
     */
    MemoryIntent parseAiResponseForIntent(String devid, UUID aiid, UUID chatId, String response);

    /**
     * Gets the current intents state for this chat
     * @param aiid   the AI ID
     * @param chatId the Chat ID
     * @return the list of current intent states
     */
    List<MemoryIntent> getCurrentIntentsStateForChat(UUID aiid, UUID chatId);

    /**
     * Updates the status of the intent to storage.
     * @param intent the intent
     */
    void updateStatus(MemoryIntent intent);

    /**
     * Deletes all memory intents currently on storage that belong to the given aiid.
     * @param aiid the AI ID
     */
    void deleteAllIntentsForAi(UUID aiid);

    /**
     * Gets the original intent for this memory intent.
     * @param devid      the dev id
     * @param aiid       the AI ID
     * @param intentName the intent name
     * @return the intent
     */
    ApiIntent getIntent(String devid, UUID aiid, String intentName);
}
