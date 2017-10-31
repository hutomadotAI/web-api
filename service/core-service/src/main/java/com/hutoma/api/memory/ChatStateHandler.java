package com.hutoma.api.memory;

import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.containers.sub.ChatState;

import org.joda.time.DateTime;

import java.util.UUID;
import javax.inject.Inject;

/**
 * Handler for chat state.
 */
public class ChatStateHandler {

    private static final String LOGFROM = "chatstatehandler";
    private final DatabaseAI databaseAi;
    private final ILogger logger;
    private final JsonSerializer jsonSerializer;

    @Inject
    public ChatStateHandler(final DatabaseAI databaseAi, final ILogger logger, final JsonSerializer jsonSerializer) {
        this.databaseAi = databaseAi;
        this.logger = logger;
        this.jsonSerializer = jsonSerializer;
    }

    public ChatState getState(final UUID devId, final UUID aiid, final UUID chatId) {
        try {
            return this.databaseAi.getChatState(devId, aiid, chatId, jsonSerializer);
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
        }
        return ChatState.getEmpty();
    }

    public void saveState(final UUID devId, final UUID chatId, final ChatState chatState) {
        try {
            chatState.setTimestamp(DateTime.now());
            if (!this.databaseAi.saveChatState(devId, chatId, chatState, jsonSerializer)) {
                this.logger.logUserErrorEvent(LOGFROM, "Could not save state for chat " + chatId,
                        devId.toString(), LogMap.map("ChatId", chatId));
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
        }
    }
}
