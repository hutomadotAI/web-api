package com.hutoma.api.memory;

import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.ChatState;
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
     * @param state    the chat state
     * @return the MemoryIntent, or null if no intent was found
     */
    MemoryIntent parseAiResponseForIntent(UUID devId, UUID aiid, UUID chatId, String response, ChatState state)
            throws ChatLogic.IntentException;

    /**
     * Gets the current intents state for this chat
     * @param state the chat state
     * @return the list of current intent states
     */
    List<MemoryIntent> getCurrentIntentsStateForChat(ChatState state);

    /**
     * Gets the original intent for this memory intent.
     * @param aiid       the AI ID
     * @param intentName the intent name
     * @return the intent
     */
    ApiIntent getIntent(UUID aiid, String intentName);

    /**
     * Resets all intents states for a given AI.
     * @param devId the developer ID
     * @param aiid  the AI ID
     */
    void resetIntentsStateForAi(UUID devId, UUID aiid);

    /**
     * Removes the given list of intents from the current list.
     * @param state           the chat state
     * @param intentsToRemove the list of intents to remove
     */
    void clearIntents(ChatState state, List<MemoryIntent> intentsToRemove);

    MemoryIntent buildMemoryIntentFromIntentName(final UUID devId,
                                                 final UUID aiid,
                                                 final String intentName,
                                                 final UUID chatId)
            throws DatabaseException, ChatLogic.IntentException;
}
