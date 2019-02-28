package com.hutoma.api.memory;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.Database;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.ChatState;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Memory Intent Handler.
 */
public class MemoryIntentHandler implements IMemoryIntentHandler {

    public static final String META_INTENT_TAG = "@meta.intent.";
    private static final String INTENT_VARIABLE_NAME_PATTERN = "%s.%s"; // <intent name>.<variable name>
    private static final Pattern META_INTEG_PATTERN =
            Pattern.compile(META_INTENT_TAG.replaceAll("\\.", "\\\\.") + "([^\\s]+)");
    private static final String LOGFROM = "intenthandler";
    private final ILogger logger;
    private final Database database;
    private final DatabaseEntitiesIntents databaseIntents;
    private final JsonSerializer jsonSerializer;


    @Inject
    public MemoryIntentHandler(final JsonSerializer jsonSerializer,
                               final DatabaseEntitiesIntents databaseIntents,
                               final ILogger logger,
                               final Database database) {
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
                                                 final String response, final ChatState state)
            throws ChatLogic.IntentException {
        try {
            if (response.trim().startsWith(META_INTENT_TAG)) {
                Matcher matcher = META_INTEG_PATTERN.matcher(response);
                if (matcher.find()) {
                    String intentName = matcher.group(1);
                    return this.loadIntentForAi(devId, aiid, chatId, intentName, state);
                }
            }
        } catch (DatabaseException de) {
            throw new ChatLogic.IntentException(de);
        }
        return null;
    }

    @Override
    public List<MemoryIntent> getCurrentIntentsStateForChat(final ChatState state) {
        return state.getCurrentIntents();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearIntents(final ChatState state, final List<MemoryIntent> intentsToRemove) {
        state.getCurrentIntents().removeAll(intentsToRemove);
    }

    private MemoryIntent loadIntentForAi(final UUID devId, final UUID aiid, final UUID chatId,
                                         final String intentName, final ChatState chatState)
            throws DatabaseException, ChatLogic.IntentException {

        MemoryIntent intent = chatState.getMemoryIntent(intentName);
        if (intent == null) {
            intent = buildMemoryIntentFromIntentName(devId, aiid, intentName, chatId);
            chatState.updateMemoryIntent(intent);
        }
        return intent;
    }

    @Override
    public MemoryIntent buildMemoryIntentFromIntentName(final UUID devId, final UUID aiid, final String intentName,
                                                        final UUID chatId)
            throws DatabaseException, ChatLogic.IntentException {
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
                ApiEntity apiEntity = this.databaseIntents.getEntity(devId, intentVar.getEntityName(), aiid);
                if (apiEntity == null) {
                    // Should not happen, but in case a rogue entity was able to squeeze through the cracks,
                    // make sure we log it and move on
                    this.logger.logUserWarnEvent(LOGFROM, "Not able to retrieve entity", devId.toString(),
                            LogMap.map("AIID", aiid).put("Entity", intentVar.getEntityName())
                                    .put("Intent", intentName));
                } else {
                    MemoryVariable variable = new MemoryVariable(
                            intentVar.getEntityName(),
                            null,
                            intentVar.isRequired(),
                            apiEntity.getEntityValueList(),
                            intentVar.getPrompts(),
                            intentVar.getNumPrompts(),
                            0,
                            apiEntity.isSystem(),
                            apiEntity.getEntityValueType(),
                            intentVar.isPersistent(),
                            intentVar.getLabel(),
                            intentVar.getClearOnEntry(),
                            intentVar.getId());
                    variables.add(variable);
                }
            }
            return new MemoryIntent(intentName, aiid, chatId, variables, false);
        }
    }

    @Override
    public void resetIntentsStateForAi(final UUID devId, final UUID aiid) {
        try {
            this.databaseIntents.resetIntentsStateForAi(devId, aiid);
        } catch (DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
    }

    public static String getPrefixedVariableName(final String intentName, final String variableName) {
        return String.format(INTENT_VARIABLE_NAME_PATTERN, intentName, variableName);
    }
}
