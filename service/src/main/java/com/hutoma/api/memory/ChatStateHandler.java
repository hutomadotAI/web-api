package com.hutoma.api.memory;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.sub.ChatState;

import org.joda.time.DateTime;

import java.util.UUID;
import javax.inject.Inject;

/**
 * Handler for chat state.
 */
public class ChatStateHandler {

    private static final String LOGFROM = "chatstatehandler";
    private final Database database;
    private final ILogger logger;

    @Inject
    public ChatStateHandler(final Database database, final ILogger logger) {
        this.database = database;
        this.logger = logger;
    }

    public ChatState getState(final String devId, final UUID chatId) {
        try {
            return this.database.getChatState(devId, chatId);
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId, ex);
        }
        return ChatState.getEmpty();
    }

    public void saveState(final String devId, final UUID chatId, final ChatState chatState) {
        try {
            chatState.setTimestamp(DateTime.now());
            if (!this.database.saveChatState(devId, chatId, chatState)) {
                this.logger.logUserErrorEvent(LOGFROM, "Could not save state for chat " + chatId,
                        devId, LogMap.map("ChatId", chatId));
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId, ex);
        }
    }
}
