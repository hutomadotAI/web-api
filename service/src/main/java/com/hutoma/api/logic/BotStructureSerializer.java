package com.hutoma.api.logic;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.IntentVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BotStructureSerializer {

    private static final int BOT_SCHEMA_VERSION = 1;

    public static BotStructure serialize(final UUID devId, final UUID aiid, final Database database,
                                  final DatabaseEntitiesIntents databaseEntitiesIntents,
                                  final JsonSerializer jsonSerializer)
            throws Database.DatabaseException {

        // Get the bot.
        ApiAi bot = database.getAI(devId, aiid, jsonSerializer);
        if (bot == null) {
            return null;
        }
        String trainingFile = database.getAiTrainingFile(aiid);
        final List<String> intentList = databaseEntitiesIntents.getIntents(devId, aiid);

        List<ApiIntent> intents = new ArrayList<>();
        HashMap<String, ApiEntity> entityMap = new HashMap<>();
        for (String intent : intentList) {
            ApiIntent apiIntent = databaseEntitiesIntents.getIntent(aiid, intent);
            intents.add(apiIntent);

            for (IntentVariable intentVariable : apiIntent.getVariables()) {
                String entityName = intentVariable.getEntityName();
                if (!entityMap.containsKey(entityName)) {
                    entityMap.put(entityName, databaseEntitiesIntents.getEntity(devId, entityName));
                }
            }
        }
        return new BotStructure(bot.getName(), bot.getDescription(), intents, trainingFile,
                entityMap, BOT_SCHEMA_VERSION, bot.getIsPrivate(), bot.getPersonality(),
                bot.getConfidence(), bot.getVoice(), bot.getLanguage().toLanguageTag(), bot.getTimezone());
    }
}
