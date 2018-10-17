package com.hutoma.api.common;

import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.IntentVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BotStructureSerializer {

    private static final int BOT_SCHEMA_VERSION = 2;

    public static BotStructure serialize(final UUID devId, final UUID aiid, final DatabaseAI database,
                                         final DatabaseEntitiesIntents databaseEntitiesIntents,
                                         final JsonSerializer jsonSerializer)
            throws DatabaseException {

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
            if (apiIntent.getWebHook() != null
                    && !apiIntent.getWebHook().isEnabled()
                    && apiIntent.getWebHook().getEndpoint().isEmpty()) {
                apiIntent.setWebHook(null);
            }
            intents.add(apiIntent);

            for (IntentVariable intentVariable : apiIntent.getVariables()) {
                String entityName = intentVariable.getEntityName();
                if (!entityMap.containsKey(entityName)) {
                    ApiEntity entity = databaseEntitiesIntents.getEntity(devId, entityName);
                    if (entity != null && !entity.isSystem()) {
                        entityMap.put(entityName, entity);
                    }
                }
            }
        }

        List<AiBot> linkedBots = database.getBotsLinkedToAi(devId, aiid);
        bot.setLinkedBots(linkedBots.stream().map(AiBot::getBotId).collect(Collectors.toList()));

        return new BotStructure(bot.getName(), bot.getDescription(), intents, trainingFile,
                entityMap, BOT_SCHEMA_VERSION, bot.getIsPrivate(), bot.getPersonality(),
                bot.getConfidence(), bot.getVoice(), bot.getLanguage().toLanguageTag(), bot.getTimezone(),
                bot.getDefaultChatResponses(), bot.getPassthroughUrl(), bot.getLinkedBots(), bot.getClient_token(),
                bot.getHandoverResetTimeoutSeconds(), bot.getErrorThresholdHandover(), bot.getHandoverMessage());
    }
}
