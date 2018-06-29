package com.hutoma.api.memory;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Handler for chat state.
 */
public class ChatStateHandlerRedis implements IChatStateHandler {

    private static final String LOGFROM = "chatstatehandler";
    // 1 hour
    private static final long CHAT_STATE_EXPIRE_SECONDS = 60 * 60;
    private final DatabaseAI databaseAi;
    private final ILogger logger;
    private final JsonSerializer jsonSerializer;
    private final RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisAsyncCommands<String, String> asyncCommands;

    @Inject
    ChatStateHandlerRedis(final DatabaseAI databaseAi, final ILogger logger, final JsonSerializer jsonSerializer) {
        this.databaseAi = databaseAi;
        this.redisClient = RedisClient.create("redis://redis-chat-state:6379/0");
        this.logger = logger;
        this.jsonSerializer = jsonSerializer;
    }

    public ChatState getState(final UUID devId, final UUID aiid, final UUID chatId) throws ChatStateException {
        this.connect();
        String chatIdString = chatId.toString();
        asyncCommands.multi();
        RedisFuture<String> aiidFuture = asyncCommands.hget(chatIdString, "aiid");
        RedisFuture<String> devidFuture = asyncCommands.hget(chatIdString, "devid");
        RedisFuture<String> chatStateFuture = asyncCommands.hget(chatIdString, "state");

        RedisFuture<TransactionResult> execResult = asyncCommands.exec();
        String chatStateString;
        try {
            TransactionResult transactionResult = execResult.get();
            String aiidRedis = aiidFuture.get();
            String devIdRedis = devidFuture.get();
            if (aiidRedis == null || devIdRedis == null) {
                validateIds(devId, aiid);
                return ChatState.getEmpty();
            }
            if (!aiidRedis.equals(aiid.toString())) {
                throw new ChatStateUserException("aiid is mismatched");
            }
            if (!devIdRedis.equals(devId.toString())) {
                throw new ChatStateUserException("devID is mismatched");
            }
            chatStateString = chatStateFuture.get();

        } catch (InterruptedException | ExecutionException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
            throw new ChatStateException("Failed to get state", ex);
        }

        ChatState state = (ChatState) jsonSerializer.deserialize(chatStateString, ChatState.class);
        return state;
    }

    public void saveState(final UUID devId, final UUID aiid, final UUID chatId, final ChatState chatState)
            throws ChatStateException {
        this.connect();
        chatState.setTimestamp(new DateTime(DateTimeZone.UTC));
        String chatIdString = chatId.toString();
        asyncCommands.multi();
        asyncCommands.hset(chatIdString, "aiid", aiid.toString());
        asyncCommands.hset(chatIdString, "devid", devId.toString());
        asyncCommands.hset(chatIdString, "state", jsonSerializer.serialize(chatState));
        asyncCommands.expire(chatIdString, CHAT_STATE_EXPIRE_SECONDS);

        RedisFuture<TransactionResult> execResult = asyncCommands.exec();
        try {
            TransactionResult transactionResult = execResult.get();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
            throw new ChatStateException("Failed to save state", ex);
        }
    }

    public void clear(final UUID devId, final UUID aiid, final UUID chatId, final ChatState chatState)
            throws ChatStateException {
        this.validateIds(devId, aiid);
        this.connect();
        asyncCommands.del(chatId.toString());
    }

    private void connect() {
        // TODO: make this thread-safe
        if (this.connection == null) {
            StatefulRedisConnection<String, String> connection = this.redisClient.connect();
            RedisAsyncCommands<String, String> commands = connection.async();
            this.connection = connection;
            this.asyncCommands = commands;
        }
    }

    private void validateIds(final UUID devId, final UUID aiid) throws ChatStateUserException {
        boolean isValid;
        try {
            isValid = this.databaseAi.checkAIBelongsToDevId(devId, aiid);
        } catch (final DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, ex.getMessage(), devId.toString(), ex);
            throw new ChatStateUserException("Database exception.");
        }

        if (!isValid) {
            throw new ChatStateUserException("Unknown AI.");
        }
    }
}
