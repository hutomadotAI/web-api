package com.hutoma.api.memory;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.ChatBaseException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Handler for chat state.
 */
public class ChatStateHandler {

    private static final String LOGFROM = "chatstatehandler";
    private final DatabaseAI databaseAi;
    private final ILogger logger;
    private final JsonSerializer jsonSerializer;

    @Inject
    ChatStateHandler(final DatabaseAI databaseAi, final ILogger logger, final JsonSerializer jsonSerializer) {
        this.databaseAi = databaseAi;
        this.logger = logger;
        this.jsonSerializer = jsonSerializer;
    }

    public ChatState getState(final UUID devId, final UUID aiid, final String serverVersion,
                              final UUID chatId) throws ChatStateException {
        ChatState state = null;
        try {
            if (!this.databaseAi.checkAIBelongsToDevId(devId, aiid)) {
                throw new ChatStateUserException("Unknown AI.");
            }
            state = this.databaseAi.getChatState(devId, aiid, chatId, jsonSerializer);
        } catch (ChatStateUserException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
            throw ex;
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
        }
        return state == null ? ChatState.getEmpty() : state;
    }

    public void saveState(final UUID devId, final UUID aiid, final UUID chatId, final ChatState chatState)
            throws ChatStateException {
        try {
            if (!this.databaseAi.checkAIBelongsToDevId(devId, aiid)) {
                throw new ChatStateUserException("Unknown AI.");
            }
            chatState.setTimestamp(new DateTime(DateTimeZone.UTC));
            if (!this.databaseAi.saveChatState(devId, chatId, chatState, jsonSerializer)) {
                this.logger.logUserErrorEvent(LOGFROM, "Could not save state for chat " + chatId,
                        devId.toString(), LogMap.map("ChatId", chatId));
            }
        } catch (ChatStateUserException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
            throw ex;
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
        }
    }

    public void clear(final UUID devId, final UUID aiid, final UUID chatId, final ChatState chatState)
            throws ChatStateException {
        chatState.setCurrentIntents(null);
        chatState.getChatContext().clear();
        chatState.setBadAnswersCount(0);
        chatState.setChatTarget(ChatHandoverTarget.Ai);
        chatState.setHistory(null);
        chatState.setLockedAiid(null);
        chatState.setTopic(null);
        saveState(devId, aiid, chatId, chatState);
    }

    public static class ChatStateException extends ChatBaseException {
        ChatStateException(final String message) {
            super(message);
        }
    }

    public static class ChatStateUserException extends ChatStateException {
        ChatStateUserException(final String message) {
            super(message);
        }
    }
}
