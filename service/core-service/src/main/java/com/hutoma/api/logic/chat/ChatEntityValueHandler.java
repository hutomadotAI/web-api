package com.hutoma.api.logic.chat;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.containers.sub.*;

import javax.inject.Inject;
import java.util.*;

public class ChatEntityValueHandler implements IChatHandler {

    private final DatabaseEntitiesIntents dbEntities;
    private final JsonSerializer serializer;
    private final EntityRecognizerService entityRecognizerService;
    private final FeatureToggler featureToggler;
    private final ILogger logger;


    @Inject
    public ChatEntityValueHandler(final DatabaseEntitiesIntents dbEntities,
                                  final JsonSerializer serializer,
                                  final EntityRecognizerService entityRecognizerService,
                                  final FeatureToggler featureToggler,
                                  final ILogger logger) {
        this.dbEntities = dbEntities;
        this.serializer = serializer;
        this.entityRecognizerService = entityRecognizerService;
        this.featureToggler = featureToggler;
        this.logger = logger;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap) {

        // Check if this feature applies
        if (featureToggler.getStateForAiid(
                requestInfo.getDevId(),
                requestInfo.getAiid(),
                "entity-value-replacement") == FeatureToggler.FeatureState.T1) {
            logger.logInfo("ChatEntityValueHandler", "entity-value-replacement feature requested");
            ERMessage toSend = new ERMessage();

            try {
                // Query DB for relevant entities and entity values
                List<Entity> entities = this.dbEntities.getEntities(requestInfo.getDevId());
                for (Entity e : entities) {
                    ApiEntity entity = this.dbEntities.getEntity(requestInfo.getDevId(), e.getName());
                    if (!entity.isSystem()) {
                        // For now only handle custom entities
                        toSend.entities.put(e.getName(), entity.getEntityValueList());
                    }
                }

                toSend.conversation = requestInfo.getQuestion();
                logger.logInfo("ChatEntityValueHandler",
                        "Entities: " + serializer.serialize(toSend));
                String conv = entityRecognizerService.findEntities(serializer.serialize(toSend));
                logger.logInfo("ChatEntityValueHandler",
                        "Entity Replacements: " + conv);

                ERMessage found = (ERMessage) serializer.deserialize(conv, ERMessage.class);

                for (Map.Entry<String, List<String>> entry : found.entities.entrySet()) {
                    // Assume only one value per entity for now
                    String entityName = entry.getKey();
                    String entityValue = entry.getValue().iterator().next();
                    currentResult.getChatState().getEntityValues().put(entityName, entityValue);
                }

                // Update conversation text
                requestInfo.setQuestion(found.conversation);

            } catch (DatabaseException ex) {
                // Log and continue
                logger.logException("Problem loading entites", ex);
            }
            return currentResult;
        } else {
            // If this feature is disabled, do nothing
            return currentResult;
        }
    }

    @Override
    public boolean chatCompleted() {
        // This never completes the chat
        return false;
    }

    private class ERMessage {
        private String conversation;
        private HashMap<String, List<String>> entities;

        ERMessage() {
            entities = new HashMap<String, List<String>>();
        }
    }

}
