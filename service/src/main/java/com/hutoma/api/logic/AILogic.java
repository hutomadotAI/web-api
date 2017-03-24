package com.hutoma.api.logic;

import com.hutoma.api.access.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import javax.inject.Inject;


/**
 * AI logic.
 */
public class AILogic {

    private static final String LOGFROM = "ailogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final AIServices aiServices;
    private final ILogger logger;
    private final Tools tools;

    @Inject
    public AILogic(final Config config, final JsonSerializer jsonSerializer, final Database database,
                   final AIServices aiServices, final ILogger logger, final Tools tools) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
        this.tools = tools;
        this.aiServices = aiServices;
    }

    public ApiResult createAI(
            final String devId,
            final String name,
            final String description,
            final boolean isPrivate,
            final int personality,
            final double confidence,
            final int voice,
            final Locale language,
            final String timezone) {
        try {
            String encodingKey = this.config.getEncodingKey();
            UUID aiUUID = this.tools.createNewRandomUUID();

            String token = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .claim("AIID", aiUUID)
                    .setSubject(devId)
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
                this.logger.logUserTraceEvent(LOGFROM, "CreateAI name clash", devId, "Name", name,
                        "Belongs to", namedAiid.toString());
                return ApiError.getBadRequest("an ai with that name already exists");
            }

            this.logger.logUserTraceEvent(LOGFROM, "CreateAI", devId, "New AIID", aiUUID.toString());
            return new ApiAi(aiUUID.toString(), token).setSuccessStatus("successfully created");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, devId, null, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateAI(
            final String devId,
            final UUID aiid,
            final String description,
            final boolean isPrivate,
            final int personality,
            final double confidence,
            final int voice,
            final Locale language,
            final String timezone) {
        try {
            ApiAi ai = this.database.getAI(devId, aiid);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "UpdateAI - AI not found", devId, "AIID", aiid.toString());
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
                    voice)) {
                this.logger.logUserErrorEvent(LOGFROM, "UpdateAI - db fail updating ai", devId,
                        "AIID", aiid.toString());
                return ApiError.getInternalServerError();
            }
            this.logger.logUserTraceEvent(LOGFROM, "UpdateAI", devId, "AIID", aiid.toString());
            return new ApiResult().setSuccessStatus("successfully updated");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "UpdateAI", devId, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getAIs(final String devId) {

        try {
            List<ApiAi> aiList = this.database.getAllAIs(devId);
            if (aiList.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "GetAIs - empty list", devId);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetAIs", devId);
            return new ApiAiList(aiList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetAIs", devId, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getSingleAI(final String devid, final UUID aiid) {
        try {
            ApiAi ai = this.database.getAI(devid, aiid);
            if (ai == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetSingleAI - not found", devid, "AIID", aiid.toString());
                return ApiError.getNotFound();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "GetSingleAI", devid, "AIID", aiid.toString());
                return ai.setSuccessStatus();
            }
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetSingleAI", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteAI(final String devid, final UUID aiid) {
        try {
            // Only delete the AI when it doesn't have a bot that has been purchased already
            AiBot bot = this.database.getPublishedBotForAI(devid, aiid);
            if (bot != null) {
                if (this.database.hasBotBeenPurchased(bot.getBotId())) {
                    this.logger.logUserTraceEvent(LOGFROM,
                            "DeleteAI - cannot delete due to bot having been purchased by others",
                            devid, "AIID", aiid.toString(), "BotId", Integer.toString(bot.getBotId()));
                    return ApiError.getBadRequest("Bot has been purchased already, cannot delete it.");
                }

                // Un-publish the bot
                this.database.updateBotPublishingState(bot.getBotId(), AiBot.PublishingState.REMOVED);
                this.logger.logUserTraceEvent(LOGFROM, "DeleteAI - unpublished bot", devid, "AIID", aiid.toString(),
                        "BotId", Integer.toString(bot.getBotId()));
            }

            if (!this.database.deleteAi(devid, aiid)) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteAI - not found", devid, "AIID", aiid.toString());
                return ApiError.getNotFound();
            }

            try {
                this.aiServices.deleteAI(devid, aiid);
            } catch (ServerConnector.AiServicesException ex) {
                if (Stream.of(ex.getSuppressed())
                        .filter(c -> c instanceof ServerConnector.AiServicesException)
                        .map(ServerConnector.AiServicesException.class::cast)
                        .allMatch(x -> x.getResponseStatus() == HttpURLConnection.HTTP_NOT_FOUND)) {
                    // all exceptions are related to AI not being found by the backends, so just ignore
                    this.logger.logUserTraceEvent(LOGFROM, "DeleteAI - Suppressed NOT FOUND errors from backends",
                            devid);
                } else {
                    // rethrow
                    throw ex;
                }
            }
            this.logger.logUserTraceEvent(LOGFROM, "DeleteAI", devid, "AIID", aiid.toString());
            return new ApiResult().setSuccessStatus("deleted successfully");
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteAI", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getLinkedBots(final String devId, final UUID aiid) {
        try {
            this.logger.logUserTraceEvent(LOGFROM, "GetLinkedBots", devId, "AIID", aiid.toString());
            return new ApiAiBotList(this.database.getBotsLinkedToAi(devId, aiid)).setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetLinkedBots", devId, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult linkBotToAI(final String devId, final UUID aiid, final int botId) {
        try {
            Map<String, Object> map = new LinkedHashMap<String, Object>() {{
                put("AIID", aiid.toString());
                put("BotId", botId);
            }};
            AiBot botDetails = this.database.getBotDetails(botId);
            if (botDetails == null) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not found", devId, map);
                return ApiError.getBadRequest(String.format("Bot %d not found", botId));
            }
            // If bot is now owned (purchased or built by the dev) then it can't be linked
            if (!botDetails.getDevId().equals(devId)) {
                List<AiBot> purchased = this.database.getPurchasedBots(devId);
                if (!purchased.stream().anyMatch(b -> b.getBotId() == botId)) {
                    this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not owned", devId, map);
                    return ApiError.getBadRequest(String.format("Bot %d not owned", botId));
                }
            }
            List<AiBot> linked = this.database.getBotsLinkedToAi(devId, aiid);
            if (linked.size() >= this.config.getMaxLinkedBotsPerAi()) {
                this.logger.logUserTraceEvent(LOGFROM,
                        "LinkBotToAI - reached maximum allowed number of bots to be linked", devId, map);
                return ApiError.getBadRequest(String.format("Maximum number of linked bots reached: %d",
                        this.config.getMaxLinkedBotsPerAi()));
            }
            if (linked.stream().anyMatch(b -> b.getBotId() == botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot already linked to AI", devId, map);
                return ApiError.getBadRequest(String.format("Bot %d already linked to AI", botId));
            }
            this.aiServices.stopTrainingIfNeeded(devId, aiid);
            if (this.database.linkBotToAi(devId, aiid, botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI", devId, map);
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "LinkBotToAI - bot not found", devId, map);
                return ApiError.getNotFound("AI or Bot not found");
            }
        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "LinkBotToAI", devId, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult unlinkBotFromAI(final String devId, final UUID aiid, final int botId) {
        try {

            this.aiServices.stopTrainingIfNeeded(devId, aiid);
            if (this.database.unlinkBotFromAi(devId, aiid, botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "UnlinkBotFromAI", devId, "AIID", aiid.toString(),
                        "BotId", botId);
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logUserTraceEvent(LOGFROM, "UnlinkBotFromAI - not found or not linked", devId,
                        "AIID", aiid.toString(),
                        "BotId", botId);
                return ApiError.getNotFound("AI or Bot not found, or not currently linked");
            }
        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "UnlinkBotFromAI", devId, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getPublishedBotForAI(final String devId, final UUID aiid) {
        try {
            AiBot bot = this.database.getPublishedBotForAI(devId, aiid);
            if (bot == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetPublishedBotForAI - not found", devId,
                        "AIID", aiid.toString());
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetPublishedBotForAI", devId, "AIID", aiid.toString());
            return new ApiAiBot(bot).setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetPublishedBotForAI", devId, ex);
            return ApiError.getInternalServerError();
        }
    }
}
