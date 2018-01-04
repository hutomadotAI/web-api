package com.hutoma.api.memory;

import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.logic.ChatLogic;

import java.util.List;
import java.util.UUID;

/**
 * Created by pedrotei on 07/10/16.
 */
public interface IMemoryIntentHandler {

    /**
     * Parses the AI response in search for an intent.
     * @param chatId   the Chat ID
     * @param response the AI response
     * @return the MemoryIntent, or null if no intent was found
     */
    MemoryIntent parseAiResponseForIntent(UUID devId, UUID aiid, UUID chatId, String response)
            throws ChatLogic.IntentException;

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
     * Clears the intents so that they can be used again.
     * @param intents the list of intents to clear
     */
    void clearIntents(List<MemoryIntent> intents);

    /**
     * Gets the original intent for this memory intent.
     * @param aiid       the AI ID
     * @param intentName the intent name
     * @return the intent
     */
    ApiIntent getIntent(UUID aiid, String intentName);
}
