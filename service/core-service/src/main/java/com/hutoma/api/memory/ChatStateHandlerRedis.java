package com.hutoma.api.memory;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import io.lettuce.core.*;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Handler for chat state.
 */
public class ChatStateHandlerRedis implements IChatStateHandler {

    private static final String LOGFROM = "chatstatehandler";
    private final ILogger logger;
    private final JsonSerializer jsonSerializer;
    private final RedisClient redisClient;

    @Inject
    ChatStateHandlerRedis(final DatabaseAI databaseAi, final ILogger logger, final JsonSerializer jsonSerializer) {
        this.redisClient = RedisClient.create("redis://redis-chat-state:6379/0");
        this.logger = logger;
        this.jsonSerializer = jsonSerializer;
    }

    public ChatState getState(final UUID devId, final UUID aiid, final UUID chatId) throws ChatStateException {
        ChatState state = null;
        return state == null ? ChatState.getEmpty() : state;
    }

    public void saveState(final UUID devId, final UUID aiid, final UUID chatId, final ChatState chatState)
            throws ChatStateException {

    }

    public void clear(final UUID devId, final UUID aiid, final UUID chatId, final ChatState chatState)
            throws ChatStateException {
    }
}
