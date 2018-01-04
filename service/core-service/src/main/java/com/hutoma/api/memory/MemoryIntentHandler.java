package com.hutoma.api.memory;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.Database;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

/**
 * Memory Intent Handler.
 */
public class MemoryIntentHandler implements IMemoryIntentHandler {

    public static final String META_INTENT_TAG = "@meta.intent.";
    private static final Pattern META_INTEG_PATTERN =
            Pattern.compile(META_INTENT_TAG.replaceAll("\\.", "\\\\.") + "([^\\s]+)");
    private static final String LOGFROM = "intenthandler";
    private final ILogger logger;
    private final Database database;
    private final DatabaseEntitiesIntents databaseIntents;
    private final JsonSerializer jsonSerializer;


    @Inject
    public MemoryIntentHandler(final JsonSerializer jsonSerializer, final DatabaseEntitiesIntents databaseIntents,
                               final ILogger logger, final Database database) {
        this.logger = logger;
        this.databaseIntents = databaseIntents;
        this.database = database;
        this.jsonSerializer = jsonSerializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MemoryIntent parseAiResponseForIntent(final UUID devId, final UUID aiid, final UUID chatId,
                                                 final String response) throws ChatLogic.IntentException {
        try {
            if (response.trim().startsWith(META_INTENT_TAG)) {
                Matcher matcher = META_INTEG_PATTERN.matcher(response);
                if (matcher.find()) {
                    String intentName = matcher.group(1);
                    return this.loadIntentForAi(devId, aiid, chatId, intentName);
                }
            }
        } catch (DatabaseException de) {
            throw new ChatLogic.IntentException(de);
        }
        return null;
    }

    @Override
    public List<MemoryIntent> getCurrentIntentsStateForChat(final UUID aiid, final UUID chatId) {
        try {
            return this.databaseIntents.getMemoryIntentsForChat(aiid, chatId, this.jsonSerializer);
        } catch (DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatus(final MemoryIntent intent) {
        try {
            this.databaseIntents.updateMemoryIntent(intent, this.jsonSerializer);
        } catch (DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllIntentsForAi(final UUID aiid) {
        try {
            this.databaseIntents.deleteAllMemoryIntents(aiid);
        } catch (DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearIntents(final List<MemoryIntent> intents) {
        try {
            for (MemoryIntent intent : intents) {
                this.databaseIntents.deleteMemoryIntent(intent);
            }
        } catch (DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiIntent getIntent(final UUID aiid, final String intentName) {
        try {
            return this.databaseIntents.getIntent(aiid, intentName);
        } catch (DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
        return null;
    }

    private MemoryIntent loadIntentForAi(final UUID devId, final UUID aiid, final UUID chatId,
                                         final String intentName) throws DatabaseException, ChatLogic.IntentException {
        MemoryIntent intent = this.databaseIntents.getMemoryIntent(intentName, aiid, chatId, this.jsonSerializer);
        if (intent == null) {
            ApiIntent apiIntent = this.databaseIntents.getIntent(aiid, intentName);
            if (apiIntent == null) {
                this.logger.logUserWarnEvent(LOGFROM, "Attempted to load non-existing intent",
                        devId.toString(),
                        LogMap.map("Intent", intentName).put("AIID", aiid)
                            .put("chatId", chatId));
                throw new ChatLogic.IntentException("Attempted to load non-existing intent");
            } else {
                List<MemoryVariable> variables = new ArrayList<>();
                // This intent is not yet available in the db, so we need to initialize it from the existing
                // intent configuration
                for (IntentVariable intentVar : apiIntent.getVariables()) {
                    ApiEntity apiEntity = this.databaseIntents.getEntity(intentVar.getDevOwner(),
                            intentVar.getEntityName());
                    MemoryVariable variable = new MemoryVariable(
                            intentVar.getEntityName(),
                            null,
                            intentVar.isRequired(),
                            apiEntity.getEntityValueList(),
                            intentVar.getPrompts(),
                            intentVar.getNumPrompts(),
                            0,
                            apiEntity.isSystem(),
                            intentVar.isPersistent(),
                            intentVar.getLabel());
                    variables.add(variable);
                }
                intent = new MemoryIntent(intentName, aiid, chatId, variables, false);
                // write it to the db
                this.databaseIntents.updateMemoryIntent(intent, this.jsonSerializer);
            }
        }
        return intent;
    }

}
