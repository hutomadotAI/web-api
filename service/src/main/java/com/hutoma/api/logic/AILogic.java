package com.hutoma.api.logic;

import com.hutoma.api.access.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.Entity;
import com.hutoma.api.containers.sub.IntentVariable;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Provider;


/**
 * AI logic.
 */
public class AILogic {

    private static final String LOGFROM = "ailogic";
    private static final int version = 1;
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final DatabaseEntitiesIntents databaseEntitiesIntents;
    private final AIServices aiServices;
    private final ILogger logger;
    private final Tools tools;
    private Provider<AIIntegrationLogic> integrationLogicProvider;

    @Inject
    public AILogic(final Config config, final JsonSerializer jsonSerializer, final Database database,
                   final DatabaseEntitiesIntents databaseEntitiesIntents,
                   final AIServices aiServices, final ILogger logger, final Tools tools,
                   Provider<AIIntegrationLogic> integrationLogicProvider) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.logger = logger;
        this.tools = tools;
        this.aiServices = aiServices;
        this.integrationLogicProvider = integrationLogicProvider;
    }

    public ApiResult createAI(
            final UUID devId,
            final String name,
            final String description,
            final boolean isPrivate,
            final int personality,
            final double confidence,
            final int voice,
            final Locale language,
            final String timezone) {
        final String devIdString = devId.toString();
        try {
            String encodingKey = this.config.getEncodingKey();
            UUID aiUUID = this.tools.createNewRandomUUID();

            String token = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .claim("AIID", aiUUID)
                    .setSubject(devIdString)
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encodingKey)
                    .compact();

            UUID namedAiid = this.database.createAI(
                    aiUUID,
                    name,
                    description,
                    devId,
                    isPrivate,
                    token,
                    language,
                    timezone,
                    confidence,
                    personality,
                    voice,
                    this.jsonSerializer);

            // if the stored procedure returns a different aiid then it didn't
            // create the one we requested because of a name clash
            if (!namedAiid.equals(aiUUID)) {
                this.logger.logUserTraceEvent(LOGFROM, "CreateAI name clash", devIdString,
                        LogMap.map("Name", name).put("Belongs to", namedAiid));
                return ApiError.getBadRequest("A bot with that name already exists");
            }

            this.logger.logUserTraceEvent(LOGFROM, "CreateAI", devIdString, LogMap.map("New AIID", aiUUID));
            return new ApiAi(aiUUID.toString(), token).setSuccessStatus("successfully created");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, devIdString, null, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateAI(
            final UUID devId,
            final UUID aiid,
            final String description,
            final boolean isPrivate,
            final int personality,
            final double confidence,
            final int voice,
            final Locale language,
            final String timezone,
            final List<String> defaultChatResponses) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            ApiAi ai = this.database.getAI(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "UpdateAI - AI not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (!this.database.updateAI(
                    devId,
                    aiid,
                    description,
                    isPrivate,
                    language,
                    timezone,
                    confidence,
                    personality,
                    voice,
                    defaultChatResponses,
                    this.jsonSerializer)) {
                this.logger.logUserErrorEvent(LOGFROM, "UpdateAI - db fail updating ai", devIdString, logMap);
                return ApiError.getInternalServerError();
            }
            this.logger.logUserTraceEvent(LOGFROM, "UpdateAI", devIdString, logMap);
            return new ApiResult().setSuccessStatus("Successfully updated");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "UpdateAI", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getAIs(final UUID devId) {
        final String devIdString = devId.toString();
        try {
            List<ApiAi> aiList = this.database.getAllAIs(devId, this.jsonSerializer);
            if (aiList.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "GetAIs - empty list", devIdString);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetAIs", devIdString);
            return new ApiAiList(aiList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetAIs", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getSingleAI(final UUID devid, final UUID aiid) {
        final String devIdString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            ApiAi ai = this.database.getAI(devid, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetSingleAI - not found", devIdString, logMap);
                return ApiError.getNotFound();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "GetSingleAI", devIdString, logMap);
                return ai.setSuccessStatus();
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetSingleAI", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult setAiBotConfigDescription(final UUID devid, final UUID aiid,
                                               AiBotConfigWithDefinition aiBotConfigWithDefinition) {
        final String devIdString = devid.toString();

        try {
            aiBotConfigWithDefinition.checkIsValid();
        } catch (AiBotConfigException ce) {
            return ApiError.getBadRequest(ce.getMessage());
        }

        AiBotConfigDefinition definition = aiBotConfigWithDefinition.getDefinitions();
        try {
            if (!this.database.setBotConfigDefinition(devid, aiid, definition, this.jsonSerializer)) {
                return ApiError.getBadRequest("Failed to write API key description");
            }

            return this.setAiBotConfig(devid, aiid, 0, aiBotConfigWithDefinition.getConfig());
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "setAiBotConfigDescription", devIdString, e);
            return ApiError.getInternalServerError();
        }

    }

    public ApiResult setAiBotConfig(final UUID devid, final UUID aiid, final int botId, AiBotConfig aiBotConfig) {
        final String devIdString = devid.toString();

        if (!aiBotConfig.isValid()) {
            return ApiError.getBadRequest("Config is not valid");
        }

        // if version is not specified, then change it to the current version
        if (aiBotConfig.getVersion() != AiBotConfig.CURRENT_VERSION) {
            aiBotConfig.setVersion(AiBotConfig.CURRENT_VERSION);
        }

        try {
            if (!this.database.checkAIBelongsToDevId(devid, aiid)) {
                return ApiError.getNotFound();
            }

            UUID linkedDevid = devid;
            UUID linkedAiid = aiid;
            if (botId != 0) {
                Pair<UUID, UUID> linkedDevidAiid = this.database.getIsBotLinkedToAi(devid, aiid, botId);
                if (linkedDevidAiid == null) {
                    return ApiError.getNotFound();
                }
                linkedDevid = linkedDevidAiid.getA();
                linkedAiid = linkedDevidAiid.getB();
            }

            AiBotConfigDefinition definition = this.database.getBotConfigDefinition(linkedDevid, linkedAiid,
                    this.jsonSerializer);
            AiBotConfigWithDefinition withDefinition = new AiBotConfigWithDefinition(aiBotConfig, definition);
            try {
                withDefinition.checkIsValid();
            } catch (AiBotConfigException configException) {
                return ApiError.getBadRequest(configException.getMessage());
            }
            if (this.database.setAiBotConfig(devid, aiid, botId, aiBotConfig, this.jsonSerializer)) {
                return new ApiResult().setSuccessStatus();
            }
            return ApiError.getBadRequest("Failed to set AI/bot config");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "setAiConfig", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult regenerateWebhookSecret(final UUID devid, final UUID aiid) {
        final String devIdString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            String newSecret = tools.generateRandomHexString(WebHooks.HMAC_SECRET_LENGTH);
            boolean isOk = this.database.setWebhookSecretForBot(aiid, newSecret);
            if (isOk) {
                this.logger.logUserTraceEvent(LOGFROM, "regenerateWebhookSecret", devIdString, logMap);
                return new ApiResult().setSuccessStatus(newSecret);
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "regenerateWebhookSecret - set webhook secret failed",
                        devIdString, logMap);
                return ApiError.getInternalServerError();
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "regenerateWebhookSecret", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteAI(final UUID devid, final UUID aiid) {
        final String devIdString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            // Only delete the AI when it doesn't have a bot that has been purchased already
            AiBot bot = this.database.getPublishedBotForAI(devid, aiid);
            if (bot != null) {
                if (this.database.hasBotBeenPurchased(bot.getBotId())) {
                    this.logger.logUserTraceEvent(LOGFROM,
                            "DeleteAI - cannot delete due to bot having been purchased by others",
                            devIdString, logMap.put("BotId", bot.getBotId()));
                    return ApiError.getBadRequest("Bot has been purchased already, cannot delete it.");
                }

                // Un-publish the bot
                this.database.updateBotPublishingState(bot.getBotId(), AiBot.PublishingState.REMOVED);
                this.logger.logUserTraceEvent(LOGFROM, "DeleteAI - unpublished bot", devIdString,
                        logMap.put("BotId", bot.getBotId()));
            }

            ApiAi ai = this.database.getAI(devid, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteAI - not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            this.database.deleteAi(devid, aiid);

            // if there are integrations, get rid of them
            this.integrationLogicProvider.get().deleteIntegrations(aiid, devid);

            try {
                this.aiServices.deleteAI(ai.getBackendStatus(), devid, aiid);
            } catch (ServerConnector.AiServicesException ex) {
                if (Stream.of(ex.getSuppressed())
                        .filter(c -> c instanceof ServerConnector.AiServicesException)
                        .map(ServerConnector.AiServicesException.class::cast)
                        .allMatch(x -> x.getResponseStatus() == HttpURLConnection.HTTP_NOT_FOUND)) {
                    // all exceptions are related to AI not being found by the backends, so just ignore
                    this.logger.logUserTraceEvent(LOGFROM, "DeleteAI - Suppressed NOT FOUND errors from backends",
                            devIdString);
                } else {
                    // rethrow
                    throw ex;
                }
            }
            this.logger.logUserTraceEvent(LOGFROM, "DeleteAI", devIdString, logMap);
            return new ApiResult().setSuccessStatus("Deleted successfully");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteAI", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getLinkedBots(final UUID devId, final UUID aiid) {
        try {
            this.logger.logUserTraceEvent(LOGFROM, "GetLinkedBots", devId.toString(), LogMap.map("AIID", aiid));
            return new ApiAiBotList(this.database.getBotsLinkedToAi(devId, aiid)).setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetLinkedBots", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getLinkedBotData(final UUID devid, final UUID aiid, final int botId) {
        final String devIdString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            ApiLinkedBotData data = this.database.getLinkedBotData(devid, aiid, botId, this.jsonSerializer);
            if (data == null) {
                this.logger.logUserTraceEvent(LOGFROM, "getLinkedBotData - not found", devIdString, logMap);
                return ApiError.getNotFound();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "getLinkedBotData", devIdString, logMap);
                return data.setSuccessStatus();
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "getLinkedBotData", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult linkBotToAI(final UUID devId, final UUID aiid, final int botId) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid).put("BotId", botId);
            AiBot botDetails = this.database.getBotDetails(botId);
            if (botDetails == null) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not found", devIdString, logMap);
                return ApiError.getBadRequest(String.format("Bot %d not found", botId));
            }
            // If bot is now owned (purchased or built by the dev) then it can't be linked
            if (!botDetails.getDevId().equals(devId)) {
                List<AiBot> purchased = this.database.getPurchasedBots(devId);
                if (!purchased.stream().anyMatch(b -> b.getBotId() == botId)) {
                    this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not owned", devIdString, logMap);
                    return ApiError.getBadRequest(String.format("Bot %d not owned", botId));
                }
            }
            List<AiBot> linked = this.database.getBotsLinkedToAi(devId, aiid);
            if (linked.size() >= this.config.getMaxLinkedBotsPerAi()) {
                this.logger.logUserTraceEvent(LOGFROM,
                        "LinkBotToAI - reached maximum allowed number of bots to be linked", devIdString, logMap);
                return ApiError.getBadRequest(String.format("Maximum number of linked bots reached: %d",
                        this.config.getMaxLinkedBotsPerAi()));
            }
            if (linked.stream().anyMatch(b -> b.getBotId() == botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot already linked to AI", devIdString, logMap);
                return ApiError.getBadRequest(String.format("Bot %d already linked to AI", botId));
            }
            this.aiServices.stopTrainingIfNeeded(devId, aiid);
            if (this.database.linkBotToAi(devId, aiid, botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI", devIdString, logMap);
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not found", devIdString, logMap);
                return ApiError.getNotFound("Bot not found");
            }
        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "LinkBotToAI", devIdString, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult unlinkBotFromAI(final UUID devId, final UUID aiid, final int botId) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid).put("BotId", botId);
            this.aiServices.stopTrainingIfNeeded(devId, aiid);
            if (this.database.unlinkBotFromAi(devId, aiid, botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "UnlinkBotFromAI", devIdString, logMap);
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "UnlinkBotFromAI - not found or not linked",
                        devIdString, logMap);
                return ApiError.getNotFound("Bot not found, or not currently linked");
            }
        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "UnlinkBotFromAI", devIdString, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getPublishedBotForAI(final UUID devId, final UUID aiid) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            AiBot bot = this.database.getPublishedBotForAI(devId, aiid);
            if (bot == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetPublishedBotForAI - not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetPublishedBotForAI", devIdString, logMap);
            return new ApiAiBot(bot).setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetPublishedBotForAI", devIdString, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult exportBotData(final UUID devId, final UUID aiid) {
        final String devIdString = devId.toString();
        try {
            // Get the bot.
            ApiAi bot = this.database.getAI(devId, aiid, this.jsonSerializer);
            String trainingFile = this.database.getAiTrainingFile(aiid);
            final List<String> intentList = this.databaseEntitiesIntents.getIntents(devId, aiid);

            List<ApiIntent> intents = new ArrayList<ApiIntent>();
            HashMap<String, ApiEntity> entityMap = new HashMap<String, ApiEntity>();
            for (String intent : intentList) {
                ApiIntent apiIntent = this.databaseEntitiesIntents.getIntent(aiid, intent);
                intents.add(apiIntent);

                for (IntentVariable intentVariable : apiIntent.getVariables()) {
                    String entityName = intentVariable.getEntityName();
                    if (!entityMap.containsKey(entityName)) {
                        entityMap.put(entityName, this.databaseEntitiesIntents.getEntity(devId, entityName));
                    }
                }
            }
            BotStructure botStructure = new BotStructure(bot.getName(), bot.getDescription(), intents, trainingFile,
                    entityMap, this.version, bot.getIsPrivate(), bot.getPersonality(), (float)bot.getConfidence(),
                    bot.getVoice(), bot.getLanguage().toString(), bot.getTimezone());
            return new ApiBotStructure(botStructure).setSuccessStatus();


        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Failed to export bot data.", devIdString, ex);
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Failed to populate bot structure for export.", devIdString, ex);
        }

        return ApiError.getInternalServerError();
    }

    public ApiResult importBot(final UUID devId, BotStructure importedBot) {
        if (importedBot == null) {
            return new ApiError();
        }

        ApiAi createdBot = null;

        // Handle specific versions.
        if (importedBot.getVersion() == 1) {
            try {
                createdBot = importV1(devId, importedBot);
            } catch (BotImportException e) {
                this.logger.logUserExceptionEvent(LOGFROM, "ImportBotV1", devId.toString(), e);
                return ApiError.getBadRequest(e.getMessage());
            }
        }

        if (createdBot == null) {
            return ApiError.getInternalServerError();
        }

        // Bot successfully imported. Start training.
        try {
            this.aiServices.startTraining(createdBot.getBackendStatus(), devId, UUID.fromString(createdBot.getAiid()));
        } catch (AIServices.AiServicesException | RuntimeException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "ImportStartTraining", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }

        return new ApiResult().setCreatedStatus();
    }


    private ApiAi importV1(UUID devId, BotStructure importedBot) throws BotImportException {
        if (!importedBot.validVersion()) {
            throw new BotImportException("Invalid Bot Structure for specified version.");
        }
        ApiResult result = this.createAI(devId, importedBot.getName(), importedBot.getDescription(), importedBot.isPrivate(),
                importedBot.getPersonality(), importedBot.getConfidence(), importedBot.getVoice(), Locale.forLanguageTag(importedBot.getLanguage()),
                importedBot.getTimezone());

        ApiAi bot = (ApiAi)result;
        if (bot == null) {
            throw new BotImportException(result.getStatus().getInfo());
        }

        UUID aiid = UUID.fromString(bot.getAiid());

        try {
            bot = (ApiAi)this.getSingleAI(devId, aiid);
        } catch (Exception e) {
            throw new BotImportException("Failed to retrieve newly imported bot.");
        }

        List<Entity> userEntities = null;
        try {
            // Add the entities that the user doesn't currently have.
            userEntities = this.databaseEntitiesIntents.getEntities(devId);
        } catch (Database.DatabaseException ex) {
            throw new BotImportException("Can't retrieve users existing entities.");
        }

        try {
            for (ApiEntity e : importedBot.getEntities().values()) {
                boolean hasEntity = false;
                for (Entity ue : userEntities) {
                    if (ue.getName().equals(e.getEntityName())) {
                        hasEntity = true;
                        break;
                    }
                }
                if (!hasEntity) {
                    this.databaseEntitiesIntents.writeEntity(devId, e.getEntityName(), e);
                }
            }
        }
        catch (Database.DatabaseException ex) {
            throw new BotImportException("Failed to create new entities from imported bot.");
        }

        // Import intents.
        try {
            for (ApiIntent intent : importedBot.getIntents()) {
                this.databaseEntitiesIntents.writeIntent(devId, UUID.fromString(bot.getAiid()), intent.getIntentName(), intent);
            }
        } catch (Database.DatabaseException ex) {
            throw new BotImportException("Failed to write intents for imported bot.");
        }

        // Add the training file.
        try {
            this.databaseEntitiesIntents.updateAiTrainingFile(aiid, importedBot.getTrainingFile());
        } catch (Exception e) {
            throw new BotImportException("Failed to add training file for imported bot.");
        }

        return bot;
    }

    static class BotImportException extends Exception {
        public BotImportException(final String message) {
            super(message);
        }
    }
}
