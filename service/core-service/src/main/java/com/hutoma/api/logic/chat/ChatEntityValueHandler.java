package com.hutoma.api.logic.chat;

import com.hutoma.api.common.FeatureToggler;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.connectors.EntityRecognizerService;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.ChatLogic;
import com.hutoma.api.logic.chat.EntityRecognizerMessage;
import com.hutoma.api.common.Tools;
import com.hutoma.api.memory.IEntityRecognizer;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatEntityValueHandler implements IChatHandler {

    private static final String LOGFROM = "ChatEntityValueHandler";
    private final DatabaseEntitiesIntents dbEntities;
    private final JsonSerializer serializer;
    private final EntityRecognizerService entityRecognizerService;
    // IEntityRecognizer ultimately calls EntityRecognizerService, will be cleaned up in a future PR
    private final IEntityRecognizer entityRecognizer;
    private final FeatureToggler featureToggler;
    private final ILogger logger;
    private final Tools tools;


    @Inject
    public ChatEntityValueHandler(final DatabaseEntitiesIntents dbEntities,
                                  final JsonSerializer serializer,
                                  final EntityRecognizerService entityRecognizerService,
                                  final IEntityRecognizer entityRecognizer,
                                  final FeatureToggler featureToggler,
                                  final ILogger logger,
                                  final Tools tools) {
        this.dbEntities = dbEntities;
        this.serializer = serializer;
        this.entityRecognizerService = entityRecognizerService;
        this.entityRecognizer = entityRecognizer;
        this.featureToggler = featureToggler;
        this.logger = logger;
        this.tools = tools;
    }

    @Override
    public ChatResult doWork(final ChatRequestInfo requestInfo,
                             final ChatResult currentResult,
                             final LogMap telemetryMap) throws ChatLogic.ChatFailedException {
        AiIdentity aiIdentity = requestInfo.getAiIdentity();

        LogMap logMap = LogMap.map("AIID", aiIdentity.getAiid().toString())
                .put("DevID", aiIdentity.getDevId().toString())
                .put("ChatId", currentResult.getChatId().toString())
                .put("Language", aiIdentity.getLanguage())
                .put("Version", aiIdentity.getServerVersion());

        EntityRecognizerMessage toSend = new EntityRecognizerMessage();

        int numberStandardEntities = 0;
        int numberRegexEntities = 0;
        try {
            // Query DB for relevant entities and entity values
            List<Entity> entities = this.dbEntities.getEntities(requestInfo.getDevId(), aiIdentity.getAiid());
            for (Entity e : entities) {
                ApiEntity entity = this.dbEntities
                        .getEntity(requestInfo.getDevId(), e.getName(), aiIdentity.getAiid());
                if (entity.getEntityValueType() == EntityValueType.LIST) {
                    // Handle custom regular entities
                    toSend.getEntities().put(e.getName(), entity.getEntityValueList());
                    numberStandardEntities++;
                } else if (entity.getEntityValueType() == EntityValueType.REGEX) {
                    // Handle regex entities if we're supposed to for this call
                        toSend.getRegexEntities().put(e.getName(), entity.getEntityValueList().get(0));
                        numberRegexEntities++;
                }
                // Don't currently handle system entities
            }

            toSend.setConversation(requestInfo.getQuestion());
            logger.logUserTraceEvent(LOGFROM,
                    "Entities to serialize",
                    requestInfo.getDevId().toString(),
                    LogMap.map("entities", serializer.serialize(toSend)));

            // Time findEntities call
            EntityRecognizerMessage candidateEntityValues = null;
            String jsonResponse = null;
            try {
                final long startTimeStamp = this.tools.getTimestamp();
                jsonResponse = entityRecognizerService.findEntities(serializer.serialize(toSend),
                     aiIdentity.getLanguage());
                // Just in case something odd has happened with the ER
                if (jsonResponse == null) {
                    throw new ChatLogic.ChatFailedException(ApiError.getInternalServerError(
                        "Empty response returned from EntityRecognizer findentities"));
                }
                // We have a response so log perf and data
                logger.logPerf(LOGFROM,
                        "findEntities call time",
                        logMap.put("Duration", this.tools.getTimestamp() - startTimeStamp)
                            .put("Number of string entities", numberStandardEntities)
                            .put("Number of regex entities", numberRegexEntities));
                logger.logUserTraceEvent(LOGFROM,
                        "Entity matches from ER",
                        requestInfo.getDevId().toString(),
                        LogMap.map("entity replacements", jsonResponse));

                candidateEntityValues = (EntityRecognizerMessage) serializer.deserialize(jsonResponse,
                        EntityRecognizerMessage.class);
            } catch (EntityRecognizerService.EntityRecognizerException ex) {
                if (ex.getReason()
                        == EntityRecognizerService.EntityRecognizerException.EntityRecognizerError.INVALID_REGEX) {
                    logger.logError(LOGFROM,
                            "Invalid regex found in stored entity",
                            logMap);
                } else {
                    logger.logError(LOGFROM,
                            "Unexpected error from EntityRecognizer",
                            logMap);
                }
            } catch (Exception ex) {
                logger.logUserExceptionEvent(LOGFROM,
                        "Problem deserializing the response from ER",
                        requestInfo.getDevId().toString(),
                        ex,
                        LogMap.map("Response", jsonResponse));
            }

            if (candidateEntityValues != null) {
                for (Map.Entry<String, List<String>> entry : candidateEntityValues.getEntities().entrySet()) {
                    String entityValue = entry.getKey();
                    List<String> entityNames = entry.getValue();

                    // Store these in chat state to revisit later
                    currentResult.getChatState().getCandidateValues().put(entityValue, entityNames);
                }
            }

        } catch (DatabaseException ex) {
            // Log and continue
            logger.logUserExceptionEvent("ChatEntityValueHandler",
                    "Problem loading entities from database",
                    requestInfo.getDevId().toString(),
                    ex);
        }
        return currentResult;
    }

    @Override
    public boolean chatCompleted() {
        // This never completes the chat
        return false;
    }

    private List<Pair<String, String>> getEntitiesFromNER(final ChatRequestInfo chatInfo,
                                                          final List<MemoryVariable> memoryVariables) {
        // At this stage we're guaranteed to have variables with different entity types
        // Attempt to retrieve entities from the question
        List<Pair<String, String>> entitiesFromNER;
        entitiesFromNER = this.entityRecognizer.retrieveEntities(chatInfo, memoryVariables);

        return entitiesFromNER;
    }

}
