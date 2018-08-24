package com.hutoma.api.logic.chat;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.sub.ChatRequestInfo;
import com.hutoma.api.containers.sub.ChatResult;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logic.ChatLogic;

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
                             final LogMap telemetryMap) throws ChatLogic.ChatFailedException {

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
                logger.logUserTraceEvent("ChatEntityValueHandler",
                        "Entities to serialize",
                        requestInfo.getDevId().toString(),
                        LogMap.map("entities", serializer.serialize(toSend)));
                String jsonResponse = entityRecognizerService.findEntities(serializer.serialize(toSend));
                // Just in case something odd has happened with the ER
                if (jsonResponse == null) {
                    throw new ChatLogic.ChatFailedException(ApiError.getInternalServerError(
                            "Empty response returned from EntityRecognizer findentities"));
                }
                logger.logUserTraceEvent("ChatEntityValueHandler",
                        "Entity matches from ER",
                        requestInfo.getDevId().toString(),
                        LogMap.map("entity replacements", jsonResponse));

                ERMessage candidateEntityValues = (ERMessage) serializer.deserialize(jsonResponse, ERMessage.class);

                for (Map.Entry<String, List<String>> entry : candidateEntityValues.entities.entrySet()) {
                    String entityValue = entry.getKey();
                    List<String> entityNames = entry.getValue();

                    // Store these in chat state to revisit later
                    currentResult.getChatState().getCandidateValues().put(entityValue, entityNames);
                }

            } catch (DatabaseException ex) {
                // Log and continue
                logger.logUserExceptionEvent("ChatEntityValueHandler",
                        "Problem loading entities from database",
                        requestInfo.getDevId().toString(),
                        ex);
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

    private static class ERMessage {
        private String conversation;
        private HashMap<String, List<String>> entities;

        ERMessage() {
            entities = new HashMap<String, List<String>>();
        }
    }

}
