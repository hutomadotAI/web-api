package com.hutoma.api.logic;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.hutoma.api.access.Role;
import com.hutoma.api.common.BotStructureSerializer;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Pair;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.WebHooks;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseMarketplace;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.Entity;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.ChatDefaultHandler;
import com.hutoma.api.validation.ParameterValidationException;
import com.hutoma.api.validation.Validate;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import org.apache.commons.lang3.StringUtils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Provider;


/**
 * AI logic.
 */
public class AILogic {

    @VisibleForTesting
    static final String BOT_RO_MESSAGE = "Bot is read-only.";
    @VisibleForTesting
    static final String LINK_BOT_NOT_OWNED_TEMPLATE = "The imported bot links to \"%s\" (id=%d) which you don't own.";
    @VisibleForTesting
    static final String LINK_BOT_NOT_EXIST_TEMPLATE = "The imported bot references a non-existing skill id (%d).";
    private static final String IMPORT_GENERIC_ERROR = "There was an error importing the bot.";
    @VisibleForTesting
    static final Locale DEFAULT_LOCALE = Locale.US;
    private static final String LOGFROM = "ailogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final DatabaseAI databaseAi;
    private final DatabaseEntitiesIntents databaseEntitiesIntents;
    private final DatabaseMarketplace databaseMarketplace;
    private final AIServices aiServices;
    private final ILogger logger;
    private final Tools tools;
    private final Validate validate;
    private final Provider<DatabaseTransaction> transactionProvider;
    private Provider<AIIntegrationLogic> integrationLogicProvider;

    @Inject
    public AILogic(final Config config, final JsonSerializer jsonSerializer, final DatabaseAI databaseAi,
                   final DatabaseEntitiesIntents databaseEntitiesIntents,
                   final DatabaseMarketplace databaseMarketplace,
                   final AIServices aiServices, final ILogger logger, final Tools tools, final Validate validate,
                   final Provider<AIIntegrationLogic> integrationLogicProvider,
                   final Provider<DatabaseTransaction> transactionProvider) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.databaseAi = databaseAi;
        this.databaseMarketplace = databaseMarketplace;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.logger = logger;
        this.tools = tools;
        this.aiServices = aiServices;
        this.validate = validate;
        this.integrationLogicProvider = integrationLogicProvider;
        this.transactionProvider = transactionProvider;
    }

    public ApiResult createAI(
            final UUID devId,
            final String name,
            final String description,
            final boolean isPrivate,
            final int personality,
            final double confidence,
            final int voice,
            final List<String> defaultChatResponses,
            final Locale language,
            final String timezone,
            final int errorThresholdHandover,
            final int handoverResetTimeout,
            final String handoverMessage) {
        return this.createAI(devId, name, description, isPrivate, personality, confidence,
                voice, language, timezone, defaultChatResponses, errorThresholdHandover,
                handoverResetTimeout, handoverMessage, null);
    }

    private ApiResult createAI(
            final UUID devId,
            final String name,
            final String description,
            final boolean isPrivate,
            final int personality,
            final double confidence,
            final int voice,
            final Locale language,
            final String timezone,
            final List<String> defaultChatResponses,
            final int errorThresholdHandover,
            final int handoverResetTimeout,
            final String handoverMessage,
            final DatabaseTransaction transaction) {
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

            UUID namedAiid = transaction == null
                    ? this.databaseAi.createAI(
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
                    defaultChatResponses,
                    errorThresholdHandover,
                    handoverResetTimeout,
                    handoverMessage,
                    this.jsonSerializer)
                    : this.databaseAi.createAI(
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
                    defaultChatResponses,
                    errorThresholdHandover,
                    handoverResetTimeout,
                    handoverMessage,
                    this.jsonSerializer,
                    transaction);

            // if the stored procedure returns a different aiid then it didn't
            // create the one we requested because of a name clash
            if (!namedAiid.equals(aiUUID)) {
                this.logger.logUserTraceEvent(LOGFROM, "CreateAI name clash", devIdString,
                        LogMap.map("Name", name).put("Belongs to", namedAiid));
                return ApiError.getBadRequest("A bot with that name already exists");
            }

            this.logger.logUserTraceEvent(LOGFROM, "CreateAI", devIdString, LogMap.map("New AIID", aiUUID));
            return this.databaseAi.getAI(devId, aiUUID, this.jsonSerializer, transaction)
                    .setSuccessStatus("successfully created");
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
            final List<String> defaultChatResponses,
            final int errorThresholdHandover,
            final int handoverResetTimeout,
            final String handoverMessage) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            ApiAi ai = this.databaseAi.getAI(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "UpdateAI - AI not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "UpdateAI - Attempt to update when in RO mode",
                        devIdString, logMap);
                return ApiError.getBadRequest(BOT_RO_MESSAGE);
            }
            if (!this.databaseAi.updateAI(
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
                    errorThresholdHandover,
                    handoverResetTimeout,
                    handoverMessage,
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
            List<ApiAi> aiList = this.databaseAi.getAllAIs(devId, this.jsonSerializer);
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
        return getSingleAI(devid, aiid, null);
    }

    private ApiResult getSingleAI(final UUID devid, final UUID aiid, final DatabaseTransaction transaction) {
        return getAI(devid, aiid, "GetSingleAI", false, transaction);
    }

    public ApiResult setAiBotConfigDescription(final UUID devid, final UUID aiid,
                                               final AiBotConfigWithDefinition aiBotConfigWithDefinition) {
        final String devIdString = devid.toString();
        LogMap logMap = LogMap.map("AIID", aiid);

        try {
            aiBotConfigWithDefinition.checkIsValid();
        } catch (AiBotConfigException ce) {
            return ApiError.getBadRequest(ce.getMessage());
        }

        AiBotConfigDefinition definition = aiBotConfigWithDefinition.getDefinitions();
        try {
            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "setAiBotConfigDescription - AI not found",
                        devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "setAiBotConfigDescription - Bot is RO", devIdString, logMap);
                return ApiError.getBadRequest(BOT_RO_MESSAGE);
            }

            if (!this.databaseAi.setBotConfigDefinition(devid, aiid, definition, this.jsonSerializer)) {
                this.logger.logUserErrorEvent(LOGFROM, "setAiBotConfigDescription - failed to write to db",
                        devIdString, logMap);
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
        LogMap logMap = LogMap.map("AIID", aiid);

        if (!aiBotConfig.isValid()) {
            return ApiError.getBadRequest("Config is not valid");
        }

        // if VERSION is not specified, then change it to the current VERSION
        if (aiBotConfig.getVersion() != AiBotConfig.CURRENT_VERSION) {
            aiBotConfig.setVersion(AiBotConfig.CURRENT_VERSION);
        }

        try {
            if (!this.databaseAi.checkAIBelongsToDevId(devid, aiid)) {
                this.logger.logUserTraceEvent(LOGFROM, "setAiConfig - AI not owned", devIdString, logMap);
                return ApiError.getNotFound();
            }

            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "setAiConfig - AI not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "setAiConfig - Bot is RO", devIdString, logMap);
                return ApiError.getBadRequest(BOT_RO_MESSAGE);
            }

            UUID linkedDevid = devid;
            UUID linkedAiid = aiid;
            if (botId != 0) {
                Pair<UUID, UUID> linkedDevidAiid = this.databaseAi.getIsBotLinkedToAi(devid, aiid, botId);
                if (linkedDevidAiid == null) {
                    return ApiError.getNotFound();
                }
                linkedDevid = linkedDevidAiid.getA();
                linkedAiid = linkedDevidAiid.getB();
            }

            AiBotConfigDefinition definition = this.databaseAi.getBotConfigDefinition(linkedDevid, linkedAiid,
                    this.jsonSerializer);
            AiBotConfigWithDefinition withDefinition = new AiBotConfigWithDefinition(aiBotConfig, definition);
            try {
                withDefinition.checkIsValid();
            } catch (AiBotConfigException configException) {
                this.logger.logUserWarnEvent(LOGFROM, "setAiConfig - invalid config", devIdString,
                        logMap.put("Message", configException.getMessage()));
                return ApiError.getBadRequest(configException.getMessage());
            }
            if (this.databaseAi.setAiBotConfig(devid, aiid, botId, aiBotConfig, this.jsonSerializer)) {
                this.logger.logUserTraceEvent(LOGFROM, "setAiConfig", devIdString,
                        logMap.put("BotId", botId));
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
            String newSecret = this.tools.generateRandomHexString(WebHooks.HMAC_SECRET_LENGTH);
            boolean isOk = this.databaseAi.setWebhookSecretForBot(aiid, newSecret);
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
            AiBot bot = this.databaseMarketplace.getPublishedBotForAI(devid, aiid);
            if (bot != null) {
                if (this.databaseMarketplace.hasBotBeenPurchased(bot.getBotId())) {
                    this.logger.logUserTraceEvent(LOGFROM,
                            "DeleteAI - cannot delete due to bot having been purchased by others",
                            devIdString, logMap.put("BotId", bot.getBotId()));
                    return ApiError.getBadRequest("Bot has been purchased already, cannot delete it.");
                }

                // Un-publish the bot
                this.databaseMarketplace.updateBotPublishingState(bot.getBotId(), AiBot.PublishingState.REMOVED);
                this.logger.logUserTraceEvent(LOGFROM, "DeleteAI - unpublished bot", devIdString,
                        logMap.put("BotId", bot.getBotId()));
            }

            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteAI - not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "setAiConfig - Bot is RO", devIdString, logMap);
                return ApiError.getBadRequest(BOT_RO_MESSAGE);
            }

            this.databaseAi.deleteAi(devid, aiid);

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
            return new ApiAiBotList(this.databaseAi.getBotsLinkedToAi(devId, aiid)).setSuccessStatus();
        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetLinkedBots", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getLinkedBotData(final UUID devid, final UUID aiid, final int botId) {
        final String devIdString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            ApiLinkedBotData data = this.databaseAi.getLinkedBotData(devid, aiid, botId, this.jsonSerializer);
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
        return this.linkBotToAI(devId, aiid, botId, null, false);
    }

    private ApiResult linkBotToAI(final UUID devId, final UUID aiid, final int botId,
                                  final DatabaseTransaction transaction, final boolean skipValidation) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid).put("BotId", botId);
            AiBot botDetails = this.databaseMarketplace.getBotDetails(botId);
            if (botDetails == null) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not found", devIdString, logMap);
                return ApiError.getBadRequest(String.format("Bot %d not found.", botId));
            }
            // If bot is now owned (purchased or built by the dev) then it can't be linked
            if (!botDetails.getDevId().equals(devId)) {
                List<AiBot> purchased = this.databaseMarketplace.getPurchasedBots(devId);
                if (purchased.stream().noneMatch(b -> b.getBotId() == botId)) {
                    this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not owned", devIdString, logMap);
                    return ApiError.getBadRequest(String.format("Bot %d not owned", botId));
                }
            }
            ApiAi ai = this.databaseAi.getAI(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - AI not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - AI is RO", devIdString, logMap);
                return ApiError.getBadRequest(BOT_RO_MESSAGE);
            }
            if (botDetails.getPublishingType() != AiBot.PublishingType.SKILL) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot is not a skill", devIdString, logMap);
                return ApiError.getBadRequest(String.format("Bot %d not a Skill", botId));
            }
            List<AiBot> linked = this.databaseAi.getBotsLinkedToAi(devId, aiid);
            if (!skipValidation && linked.size() >= this.config.getMaxLinkedBotsPerAi()) {
                this.logger.logUserTraceEvent(LOGFROM,
                        "LinkBotToAI - reached maximum allowed number of bots to be linked", devIdString, logMap);
                return ApiError.getBadRequest(String.format("Maximum number of linked bots reached: %d",
                        this.config.getMaxLinkedBotsPerAi()));
            }
            if (linked.stream().anyMatch(b -> b.getBotId() == botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot already linked to AI", devIdString, logMap);
                return ApiError.getBadRequest(String.format("Bot %d already linked to AI.", botId));
            }
            if (this.databaseAi.linkBotToAi(devId, aiid, botId, transaction)) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI", devIdString, logMap);
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not found", devIdString, logMap);
                return ApiError.getNotFound("Bot not found.");
            }
        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "LinkBotToAI", devIdString, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateLinkedBots(final UUID devId, final UUID aiid, final List<Integer> botList) {
        String devIdString = devId.toString();

        try {
            LogMap logMap = LogMap.map("AIID", aiid);

            // Check for duplicate elements in the list of bots to link
            Set<Integer> botSet = new HashSet<>(botList);
            if (botSet.size() < botList.size()) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - repeated elements", devIdString, logMap);
                return ApiError.getBadRequest("List of bots to link cannot have repeated elements.");
            }

            // Check if this AI can be updated
            ApiAi ai = this.databaseAi.getAI(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "updateLinkedBots - AI not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "updateLinkedBots - AI is RO", devIdString, logMap);
                return ApiError.getBadRequest(BOT_RO_MESSAGE);
            }

            // Determine which bots to link and unlink
            List<AiBot> botsLinked = this.databaseAi.getBotsLinkedToAi(devId, aiid);
            List<AiBot> botsToRemove = botsLinked.stream().filter(x -> !botSet.contains(x.getBotId()))
                    .collect(Collectors.toList());
            List<Integer> botIdsLinked = botsLinked.stream().map(AiBot::getBotId).collect(Collectors.toList());
            List<Integer> botsToAdd = new ArrayList<>(botList);
            botsToAdd.removeAll(botIdsLinked);

            int finalNumLinkedBots = botsLinked.size() - botsToRemove.size() + botsToAdd.size();
            if (finalNumLinkedBots > this.config.getMaxLinkedBotsPerAi()) {
                this.logger.logUserTraceEvent(LOGFROM,
                        "updateLinkedBots - trying to exceed the limit of linked bots", devIdString,
                        logMap.put("Count", finalNumLinkedBots).put("Max", this.config.getMaxLinkedBotsPerAi())
                                .put("Already linked", StringUtils.join(botIdsLinked, ","))
                                .put("Would add", StringUtils.join(botsToAdd, ","))
                                .put("Would remove", StringUtils.join(
                                        botsToRemove.stream().map(AiBot::getBotId).collect(Collectors.toList()), ",")));
                return ApiError.getBadRequest(String.format(
                        "Requested links (%d) would go over the the limit of %d linked bots.",
                        finalNumLinkedBots,
                        this.config.getMaxLinkedBotsPerAi()));
            }

            try (DatabaseTransaction transaction = this.transactionProvider.get()) {

                for (AiBot bot : botsToRemove) {
                    ApiResult result = this.unlinkBotFromAI(devId, aiid, bot.getBotId(), transaction);
                    if (result.getStatus().getCode() != HttpURLConnection.HTTP_OK) {
                        transaction.rollback();
                        return result;
                    }
                }

                for (Integer botId : botsToAdd) {
                    ApiResult result = this.linkBotToAI(devId, aiid, botId, transaction, true);
                    if (result.getStatus().getCode() != HttpURLConnection.HTTP_OK) {
                        transaction.rollback();
                        return result;
                    }
                }

                transaction.commit();

                this.logger.logUserTraceEvent(LOGFROM, "updateLinkedBots", devIdString, logMap);
                return new ApiResult().setSuccessStatus();
            }

        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "updateLinkedBots", devIdString, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult unlinkBotFromAI(final UUID devId, final UUID aiid, final int botId) {
        return this.unlinkBotFromAI(devId, aiid, botId, null);
    }

    private ApiResult unlinkBotFromAI(final UUID devId, final UUID aiid, final int botId,
                                      final DatabaseTransaction transaction) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid).put("BotId", botId);
            ApiAi ai = this.databaseAi.getAI(devId, aiid, this.jsonSerializer);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "UnlinkBotFromAI - AI not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "UnlinkBotFromAI - AI is RO", devIdString, logMap);
                return ApiError.getBadRequest(BOT_RO_MESSAGE);
            }

            if (this.databaseAi.unlinkBotFromAi(devId, aiid, botId, transaction)) {
                this.logger.logUserTraceEvent(LOGFROM, "UnlinkBotFromAI", devIdString, logMap);
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "UnlinkBotFromAI - not found or not linked",
                        devIdString, logMap);
                return ApiError.getNotFound("Bot not found, or not currently linked.");
            }
        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "UnlinkBotFromAI", devIdString, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getPublishedBotForAI(final UUID devId, final UUID aiid) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            AiBot bot = this.databaseMarketplace.getPublishedBotForAI(devId, aiid);
            if (bot == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetPublishedBotForAI - not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetPublishedBotForAI", devIdString, logMap);
            return new ApiAiBot(bot).setSuccessStatus();
        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetPublishedBotForAI", devIdString, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult exportBotData(final UUID devId, final UUID aiid) {
        final String devIdString = devId.toString();
        try {
            BotStructure botStructure = BotStructureSerializer.serialize(
                    devId, aiid, this.databaseAi, this.databaseEntitiesIntents, this.jsonSerializer);

            LogMap logMap = LogMap.map("AIID", aiid);
            if (botStructure == null) {
                this.logger.logUserTraceEvent(LOGFROM, "Export bot - not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "Export bot", devIdString, logMap);
            return new ApiBotStructure(botStructure).setSuccessStatus();

        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Failed to export bot data.", devIdString, ex);
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Failed to populate bot structure for export.", devIdString, ex);
        }

        return ApiError.getInternalServerError();
    }

    public ApiResult importBot(final UUID devId, final BotStructure importedBot) {
        if (importedBot == null) {
            return ApiError.getBadRequest();
        }

        ApiAi createdBot;
        try {
            createdBot = createImportedBot(devId, importedBot);
        } catch (BotImportException e) {
            return ApiError.getBadRequest(e.getMessage());
        }

        UUID uuidAiid = UUID.fromString(createdBot.getAiid());
        try {
            String trainingMaterials = this.aiServices.getTrainingMaterialsCommon(devId, uuidAiid, this.jsonSerializer);
            this.aiServices.uploadTraining(createdBot.getBackendStatus(), devId, uuidAiid, trainingMaterials);
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "BotImportTraining", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }

        // Bot successfully imported. Start training.
        try {
            this.aiServices.startTraining(createdBot.getBackendStatus(), devId, uuidAiid);
        } catch (AIServices.AiServicesException | RuntimeException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "ImportStartTraining", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }

        // load the new bot as a get
        ApiResult result = getAI(devId, uuidAiid, "ImportBot-GetAI", true, null);

        if (result.getStatus().getCode() == HttpURLConnection.HTTP_CREATED) {
            // Log the import
            ApiAi ai = (ApiAi) result;
            this.logger.logUserInfoEvent(LOGFROM, "ImportBot", devId.toString(),
                    LogMap.map("NewAIID", ai.getAiid())
                            .put("Name", ai.getName() == null ? "" : ai.getName())
                            .put("NumLinkedBots", ai.getLinkedBots() == null ? 0 : ai.getLinkedBots().size())
                            .put("LinkedBots", ai.getLinkedBots() == null ? "" : ai.getLinkedBots().stream()
                                    .map(Object::toString).collect(Collectors.joining(",")))
                            .put("BackendStatus", ai.getBackendStatus())
                            .put("DefaultChatResponses", ai.getDefaultChatResponses().stream()
                                    .collect(Collectors.joining(",")))
                            .put("TrainingFileSize", importedBot.getTrainingFile() == null
                                    ? 0 : importedBot.getTrainingFile().length())
                            .put("NumIntents", importedBot.getIntents() == null ? 0 : importedBot.getIntents().size())
                            .put("NumEntities", importedBot.getEntities() == null
                                    ? 0 : importedBot.getEntities().size())
                            .put("Confidence", ai.getConfidence())
                            .put("PassthroughUrl", ai.getPassthroughUrl() == null ? "" : ai.getPassthroughUrl())
                            .put("Description", ai.getDescription() == null ? "" : ai.getDescription())
                            .put("Language", importedBot.getLanguage() == null ? "" : importedBot.getLanguage())
                            .put("Timezone", importedBot.getTimezone() == null ? "" : importedBot.getTimezone())
                            .put("Personality", importedBot.getPersonality())
                            .put("Voice", importedBot.getVoice())
                            .put("Version", importedBot.getVersion()));
        }
        return result;
    }

    public ApiResult cloneBot(final UUID devId, final UUID aiidToClone,
                              final String newName,
                              final String newDescription,
                              final boolean isPrivate,
                              final int newPersonality,
                              final double newConfidence,
                              final int newVoice,
                              final Locale newLanguage,
                              final String newTimezone,
                              final List<String> defaultResponses,
                              final String passthroughUrl) {
        ApiResult result = this.exportBotData(devId, aiidToClone);
        if (result.getStatus().getCode() != HttpURLConnection.HTTP_OK) {
            return result;
        }

        BotStructure botStructure = ((ApiBotStructure) result).getBotStructure();
        String originalName = botStructure.getName();

        if (originalName.equalsIgnoreCase(newName)) {
            // Name always has to be overridden as we don't support duplicate names
            botStructure.setName(botStructure.getName() + generateBotNameRandomSuffix());
        } else {
            botStructure.setName(newName);
        }
        botStructure.setDescription(newDescription);
        botStructure.setPrivate(isPrivate);
        botStructure.setPersonality(newPersonality);
        botStructure.setConfidence(newConfidence);
        botStructure.setVoice(newVoice);
        botStructure.setLanguage(newLanguage.toLanguageTag());
        botStructure.setTimezone(newTimezone);
        botStructure.setDefaultResponses(defaultResponses);
        botStructure.setPassthroughUrl(passthroughUrl);

        return this.importBot(devId, botStructure);
    }

    static String generateBotNameRandomSuffix() {
        final String saltChars = "abcdefghijklmnopqrstuvwxyz1234567890";
        final int suffixLength = 6;
        StringBuilder salt = new StringBuilder();
        salt.append("_");
        Random rnd = new Random();
        while (salt.length() < suffixLength) { // length of the random string.
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    private ApiResult getAI(final UUID devid, final UUID aiid, String logTag, boolean isCreate,
                            final DatabaseTransaction transaction) {
        final String devIdString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            ApiAi ai = transaction == null
                    ? this.databaseAi.getAI(devid, aiid, this.jsonSerializer)
                    : this.databaseAi.getAI(devid, aiid, this.jsonSerializer, transaction);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM,
                        String.format("%s - not found", logTag), devIdString, logMap);
                return ApiError.getNotFound();
            } else {
                List<AiBot> linkedBots = this.databaseAi.getBotsLinkedToAi(devid, aiid, transaction);
                if (!linkedBots.isEmpty()) {
                    ai.setLinkedBots(linkedBots.stream().map(AiBot::getBotId).collect(Collectors.toList()));
                }
                this.logger.logUserTraceEvent(LOGFROM, logTag, devIdString, logMap);
                return isCreate ? ai.setCreatedStatus() : ai.setSuccessStatus();
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, logTag, devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    @VisibleForTesting
    ApiAi createImportedBot(final UUID devId, final BotStructure importedBot) throws BotImportException {
        // try to interpret the locale
        Locale locale;
        try {
            locale = validate.validateLocale("locale", importedBot.getLanguage());
        } catch (ParameterValidationException e) {
            // if the local is missing or badly formatted then use en-US
            locale = DEFAULT_LOCALE;
        }

        boolean hasLinkedSkills = importedBot.getLinkedSkills() != null && !importedBot.getLinkedSkills().isEmpty();

        try {
            if (hasLinkedSkills) {
                // validate that the user owns all linked skills otherwise bail out
                Set<Integer> purchasedBots = this.databaseMarketplace.getPurchasedBots(devId)
                        .stream().map(AiBot::getBotId).collect(Collectors.toSet());

                for (Integer linkedSkill : importedBot.getLinkedSkills()) {
                    if (!purchasedBots.contains(linkedSkill)) {
                        // If this is a real bot in the marketplace, at least give the user
                        // some info on how to solve the problem
                        AiBot missingBot = this.databaseMarketplace.getBotDetails(linkedSkill);
                        if (missingBot != null) {
                            this.logger.logUserInfoEvent(LOGFROM, "ImportBot - attempt to link skill not owned",
                                    devId.toString(), LogMap.map("BotId",
                                            missingBot.getBotId()).put("BotName", missingBot.getName()));
                            throw new BotImportException(String.format(LINK_BOT_NOT_OWNED_TEMPLATE,
                                    missingBot.getName(), missingBot.getBotId()));
                        } else {
                            this.logger.logUserInfoEvent(LOGFROM, "ImportBot - attempt to link non existing skill",
                                    devId.toString(), LogMap.map("BotId", linkedSkill));
                            throw new BotImportException(String.format(LINK_BOT_NOT_EXIST_TEMPLATE, linkedSkill));
                        }
                    }
                }
            }
        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "ImportBot - check linked bot ownership",
                    devId.toString(), ex);
            throw new BotImportException("There was an internal problem importing the bot.");
        }

        ApiAi bot;

        try (DatabaseTransaction transaction = this.transactionProvider.get()) {

            ApiResult result = this.createAI(
                    devId,
                    importedBot.getName(),
                    importedBot.getDescription(),
                    importedBot.isPrivate(),
                    importedBot.getPersonality(),
                    importedBot.getConfidence(),
                    importedBot.getVoice(),
                    locale,
                    importedBot.getTimezone(),
                    importedBot.getDefaultResponses(),
                    // TODO: not adding handover-related stuff from linked bots since this will soon be changed
                    // so adding default configuration
                    -1,
                    -1,
                    null,
                    transaction);

            if (result.getStatus().getCode() != HttpURLConnection.HTTP_OK) {
                this.logger.logUserErrorEvent(LOGFROM, "ImportBot - create ai", devId.toString(),
                        LogMap.map("ErrorCode", result.getStatus().getCode())
                                .put("Message", result.getStatus().getInfo()));
                // The info from an error on CreateAI should already be customer-friendly, so just
                // pass it back to the user if there is already one.
                throw new BotImportException(Strings.isNullOrEmpty(result.getStatus().getInfo())
                        ? IMPORT_GENERIC_ERROR
                        : result.getStatus().getInfo());
            }

            try {
                bot = (ApiAi) result;
            } catch (ClassCastException e) {
                this.logger.logUserExceptionEvent(LOGFROM, "ImportBot - create ai", devId.toString(), e);
                throw new BotImportException(IMPORT_GENERIC_ERROR);
            }

            UUID aiid = UUID.fromString(bot.getAiid());
            LogMap logMap = LogMap.map("AIID", aiid);

            try {
                bot = (ApiAi) this.getSingleAI(devId, aiid, transaction);
            } catch (Exception e) {
                this.logger.logUserExceptionEvent(LOGFROM, "ImportBot - retrieve ai", devId.toString(),
                        e, logMap);
                throw new BotImportException(IMPORT_GENERIC_ERROR);
            }

            if (!this.databaseAi.updatePassthroughUrl(devId, aiid, importedBot.getPassthroughUrl(), transaction)) {
                this.logger.logUserErrorEvent(LOGFROM, "ImportBot - update passthrough url", devId.toString(),
                        logMap);
                throw new BotImportException(IMPORT_GENERIC_ERROR);
            }

            List<String> defaultResponses =
                    (importedBot.getDefaultResponses() == null || importedBot.getDefaultResponses().isEmpty())
                            ? Collections.singletonList(ChatDefaultHandler.COMPLETELY_LOST_RESULT)
                            : importedBot.getDefaultResponses();
            if (!this.databaseAi.updateDefaultChatResponses(devId, aiid, defaultResponses,
                    this.jsonSerializer, transaction)) {
                this.logger.logUserErrorEvent(LOGFROM, "ImportBot - update default responses", devId.toString(),
                        logMap);
                throw new BotImportException(IMPORT_GENERIC_ERROR);
            }

            List<Entity> userEntities;
            try {
                // Add the entities that the user doesn't currently have.
                userEntities = this.databaseEntitiesIntents.getEntities(devId);
            } catch (DatabaseException ex) {
                this.logger.logUserExceptionEvent(LOGFROM, "ImportBot - retrieving existing entities ai",
                        devId.toString(), ex, logMap);
                throw new BotImportException(IMPORT_GENERIC_ERROR);
            }

            if (importedBot.getEntities() != null && !importedBot.getEntities().isEmpty()) {
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
                            this.databaseEntitiesIntents.writeEntity(devId, e.getEntityName(), e, transaction);
                        }
                    }
                } catch (DatabaseException ex) {
                    this.logger.logUserExceptionEvent(LOGFROM, "ImportBot - create entities", devId.toString(),
                            ex, logMap);
                    throw new BotImportException(IMPORT_GENERIC_ERROR);
                }
            }

            // Import intents.
            if (importedBot.getIntents() != null && !importedBot.getIntents().isEmpty()) {
                try {
                    for (ApiIntent intent : importedBot.getIntents()) {
                        UUID botAiid = UUID.fromString(bot.getAiid());
                        this.databaseEntitiesIntents.writeIntent(devId, botAiid,
                                intent.getIntentName(), intent, transaction);
                        WebHook webHook = intent.getWebHook();
                        if (webHook != null) {
                            if (!this.databaseEntitiesIntents.createWebHook(botAiid, webHook.getIntentName(),
                                    webHook.getEndpoint(), webHook.isEnabled(), transaction)) {
                                this.logger.logUserErrorEvent(LOGFROM, "ImportBot - create webhook", devId.toString(),
                                        logMap);
                                throw new BotImportException(IMPORT_GENERIC_ERROR);
                            }
                        }
                    }
                } catch (DatabaseException ex) {
                    this.logger.logUserExceptionEvent(LOGFROM, "ImportBot - write intents", devId.toString(),
                            ex, logMap);
                    throw new BotImportException(IMPORT_GENERIC_ERROR);
                }
            }

            if (importedBot.getTrainingFile() != null) {
                // Add the training file to the database
                try {
                    this.databaseAi.updateAiTrainingFile(aiid, importedBot.getTrainingFile(), transaction);
                } catch (Exception ex) {
                    this.logger.logUserExceptionEvent(LOGFROM, "ImportBot - add training file", devId.toString(),
                            ex, logMap);
                    throw new BotImportException(IMPORT_GENERIC_ERROR);
                }
            }

            // Link skills
            if (hasLinkedSkills) {
                for (Integer linkedSkill : importedBot.getLinkedSkills()) {
                    this.databaseAi.linkBotToAi(devId, aiid, linkedSkill, transaction);
                }
            }

            transaction.commit();
        } catch (DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "ImportBot - commit transaction", devId.toString(),
                    ex);
            throw new BotImportException(IMPORT_GENERIC_ERROR);
        }

        return bot;
    }

    static class BotImportException extends Exception {
        BotImportException(final String message) {
            super(message);
        }
    }
}
