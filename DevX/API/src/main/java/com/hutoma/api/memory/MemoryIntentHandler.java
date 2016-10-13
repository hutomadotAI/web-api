package com.hutoma.api.memory;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.MemoryIntent;
import com.hutoma.api.containers.sub.MemoryVariable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pedrotei on 05/10/16.
 */
public class MemoryIntentHandler implements IMemoryIntentHandler {

    public static final String META_INTENT_TAG = "@meta.intent.";
    private static final Pattern META_INTEG_PATTERN =
            Pattern.compile(META_INTENT_TAG.replaceAll("\\.", "\\\\.") + "([^\\s]+)");
    private static final String LOGFROM = "intenthandler";
    private final ILogger logger;
    private final Database database;
    private final JsonSerializer jsonSerializer;


    @Inject
    public MemoryIntentHandler(final JsonSerializer jsonSerializer, final Database database, final ILogger logger) {
        this.logger = logger;
        this.database = database;
        this.jsonSerializer = jsonSerializer;

    }

    /**
     * {@inheritDoc}
     */
    public MemoryIntent parseAiResponseForIntent(final String devid, final UUID aaid, final UUID chatId, final String response) {
        if (response.trim().startsWith(META_INTENT_TAG)) {
            Matcher m = META_INTEG_PATTERN.matcher(response);
            if (m.find()) {
                String intentName = m.group(1);
                return this.loadIntentForAi(devid, aaid, chatId, intentName);
            }
        }
        return null;
    }

    public List<MemoryIntent> getCurrentIntentsStateForChat(final UUID aiid, final UUID chatId) {
        try {
            return this.database.getMemoryIntentsForChat(aiid, chatId, this.jsonSerializer);
        } catch (Database.DatabaseException e) {
            this.logger.logError(LOGFROM, e.getMessage());
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
            this.logger.logError(LOGFROM, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAllIntentsForAi(final UUID aiid) {
        try {
            this.database.deleteAllMemoryIntents(aiid);
        } catch (Database.DatabaseException e) {
            this.logger.logError(LOGFROM, e.getMessage());
        }
    }

    private MemoryIntent loadIntentForAi(final String devid, final UUID aiid, final UUID chatId,
                                         final String intentName) {
        MemoryIntent intent = null;
        try {
            intent = this.database.getMemoryIntent(intentName, aiid, chatId, this.jsonSerializer);
            if (intent == null) {
                ApiIntent apiIntent = this.database.getIntent(devid, aiid, intentName);
                List<MemoryVariable> variables = new ArrayList<>();
                // This intent is not yet available in the db, so we need to initialize it from the existing
                // intent configuration
                for (IntentVariable intentVar : apiIntent.getVariables()) {
                    ApiEntity apiEntity = this.database.getEntity(devid, intentVar.getEntityName());
                    MemoryVariable v = new MemoryVariable(intentVar.getEntityName(), apiEntity.getEntityList());
                    v.setPrompts(intentVar.getPrompts());
                    v.setIsMandatory(intentVar.isRequired());
                    v.setTimesPrompted(intentVar.getNumPrompts());
                    variables.add(v);
                }
                intent = new MemoryIntent(intentName, aiid, chatId, variables);
                // write it to the db
                this.database.updateMemoryIntent(intent, this.jsonSerializer);
            }
        } catch (Database.DatabaseException e) {
            this.logger.logError(LOGFROM, e.getMessage());
        }
        return intent;
    }

}
