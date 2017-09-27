package com.hutoma.api.memory;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public MemoryIntent parseAiResponseForIntent(final UUID aiid, final UUID chatId,
                                                 final String response) {
        if (response.trim().startsWith(META_INTENT_TAG)) {
            Matcher matcher = META_INTEG_PATTERN.matcher(response);
            if (matcher.find()) {
                String intentName = matcher.group(1);
                return this.loadIntentForAi(aiid, chatId, intentName);
            }
        }
        return null;
    }

    public List<MemoryIntent> getCurrentIntentsStateForChat(final UUID aiid, final UUID chatId) {
        try {
            return this.database.getMemoryIntentsForChat(aiid, chatId, this.jsonSerializer);
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void updateStatus(final MemoryIntent intent) {
        try {
            this.database.updateMemoryIntent(intent, this.jsonSerializer);
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAllIntentsForAi(final UUID aiid) {
        try {
            this.database.deleteAllMemoryIntents(aiid);
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearIntents(final List<MemoryIntent> intents) {
        try {
            for (MemoryIntent intent : intents) {
                this.databaseIntents.deleteMemoryIntent(intent);
            }
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ApiIntent getIntent(final UUID aiid, final String intentName) {
        try {
            return this.databaseIntents.getIntent(aiid, intentName);
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
        return null;
    }

    private MemoryIntent loadIntentForAi(final UUID aiid, final UUID chatId,
                                         final String intentName) {
        MemoryIntent intent = null;
        try {
            intent = this.database.getMemoryIntent(intentName, aiid, chatId, this.jsonSerializer);
            if (intent == null) {
                ApiIntent apiIntent = this.databaseIntents.getIntent(aiid, intentName);
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
                this.database.updateMemoryIntent(intent, this.jsonSerializer);
            } else {
                // DEBUG - Check if there are any variables with duplicate labels
                Set<String> usedLabels = new HashSet<>();
                List<MemoryVariable> variables = intent.getVariables();
                if (variables != null) {
                    intent.getVariables().forEach(x -> {
                        if (usedLabels.contains(x.getLabel())) {
                            this.logger.logWarning(LOGFROM, "Duplicate labels",
                                    LogMap.map("Label", x.getLabel()).put("AIID", aiid).put("IntentName", intentName));
                        } else {
                            usedLabels.add(x.getLabel());
                        }
                    });
                }
            }
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
        }
        return intent;
    }

}
