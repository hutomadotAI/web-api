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
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;


/**
 * Created by mauriziocibelli on 27/04/16.
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
    public AILogic(Config config, JsonSerializer jsonSerializer, Database database, AIServices aiServices,
                   ILogger logger, Tools tools) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
        this.tools = tools;
        this.aiServices = aiServices;
    }

    public ApiResult createAI(
            SecurityContext securityContext,
            String devid,
            String name,
            String description,
            boolean isPrivate,
            int personality,
            double confidence,
            int voice,
            Locale language,
            String timezone) {
        try {
            this.logger.logDebug(LOGFROM, "request to create new ai from " + devid);

            String encodingKey = this.config.getEncodingKey();
            UUID aiUUID = this.tools.createNewRandomUUID();

            String token = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .claim("AIID", aiUUID)
                    .setSubject(devid)
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encodingKey)
                    .compact();

            // this creates an empty container for multiple-backend statuses
            // which is the same as setting each server to AI_UNDEFINED
            BackendStatus statusNew = new BackendStatus();

            UUID namedAiid = this.database.createAI(
                    aiUUID,
                    name,
                    description,
                    devid,
                    isPrivate,
                    statusNew,
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
                this.logger.logInfo(LOGFROM, "ai nameclash. name " + name + " already belongs to ai " + namedAiid);
                return ApiError.getBadRequest("an ai with that name already exists");
            }

            return new ApiAi(aiUUID.toString(), token).setSuccessStatus("successfully created");
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateAIStatus(final SecurityContext securityContext, final AiStatus status) {
        try {
            // Check if any of the backends sent a rogue double, as MySQL does not handle NaN
            if (Double.isNaN(status.getTrainingError()) || Double.isNaN(status.getTrainingProgress())) {
                return ApiError.getBadRequest("Double sent is NaN");
            }
            if (!this.database.updateAIStatus(status, this.jsonSerializer)) {
                return ApiError.getNotFound();
            }
            return new ApiResult().setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateAI(
            SecurityContext securityContext,
            String devid,
            UUID aiid,
            String description,
            boolean isPrivate,
            int personality,
            double confidence,
            int voice,
            Locale language,
            String timezone) {
        try {
            this.logger.logDebug(LOGFROM, "request to update ai " + aiid);

            if (!this.database.updateAI(
                    devid,
                    aiid,
                    description,
                    isPrivate,
                    language,
                    timezone,
                    confidence,
                    personality,
                    voice)) {
                this.logger.logInfo(LOGFROM, "db fail updating ai");
                return ApiError.getInternalServerError();
            }
            return new ApiResult().setSuccessStatus("successfully updated");
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getAIs(
            SecurityContext securityContext,
            String devid) {

        try {
            this.logger.logDebug(LOGFROM, "request to list all ais");
            List<ApiAi> aiList = this.database.getAllAIs(devid, this.jsonSerializer);
            if (aiList.isEmpty()) {
                this.logger.logDebug(LOGFROM, "ai list is empty");
                return ApiError.getNotFound();
            }
            return new ApiAiList(aiList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getSingleAI(
            SecurityContext securityContext,
            String devid,
            UUID aiid) {

        try {
            this.logger.logDebug(LOGFROM, devid + " request to list " + aiid);
            ApiAi ai = this.database.getAI(devid, aiid, this.jsonSerializer);
            if (null == ai) {
                this.logger.logDebug(LOGFROM, "ai not found");
                return ApiError.getNotFound();
            } else {
                return ai.setSuccessStatus();
            }
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteAI(final String devid, final UUID aiid) {
        try {
            this.logger.logDebug(LOGFROM, devid + " request to delete " + aiid);
            if (!this.database.deleteAi(devid, aiid)) {
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
                    this.logger.logInfo(LOGFROM, "Suppressed NOT FOUND errors from backends for AI Delete");
                } else {
                    // rethrow
                    throw ex;
                }
            }
            return new ApiResult().setSuccessStatus("deleted successfully");
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getLinkedBots(final String devId, final UUID aiid) {
        try {
            this.logger.logDebug(LOGFROM, "request to list linked bots for AI " + aiid);
            return new ApiAiBotList(this.database.getBotsLinkedToAi(devId, aiid)).setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult linkBotToAI(final String devId, final UUID aiid, final int botId) {
        try {
            this.logger.logDebug(LOGFROM, String.format("request to link bot %d to AI %s", botId, aiid));
            if (this.database.getBotDetails(botId) == null) {
                return ApiError.getBadRequest(String.format("Bot %d not found", botId));
            }
            List<AiBot> purchased = this.database.getPurchasedBots(devId);
            if (!purchased.stream().anyMatch(b -> b.getBotId() == botId)) {
                return ApiError.getBadRequest(String.format("Bot %d not owned", botId));
            }
            List<AiBot> linked = this.database.getBotsLinkedToAi(devId, aiid);
            if (linked.stream().anyMatch(b -> b.getBotId() == botId)) {
                return ApiError.getBadRequest(String.format("Bot %d already linked to AI", botId));
            }
            this.stopTrainingIfNeeded(devId, aiid);
            if (this.database.linkBotToAi(devId, aiid, botId)) {
                return new ApiResult().setSuccessStatus();
            } else {
                return ApiError.getNotFound("AI or Bot not found");
            }
        } catch (Database.DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult unlinkBotFromAI(final String devId, final UUID aiid, final int botId) {
        try {
            this.logger.logDebug(LOGFROM, String.format("request to unlink bot %d from AI %s", botId, aiid));
            this.stopTrainingIfNeeded(devId, aiid);

            if (this.database.unlinkBotFromAi(devId, aiid, botId)) {
                return new ApiResult().setSuccessStatus();
            } else {
                return ApiError.getNotFound("AI or Bot not found, or not currently linked");
            }
        } catch (Database.DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getPublishedBotForAI(final String devId, final UUID aiid) {
        try {
            this.logger.logDebug(LOGFROM, "request for the published bot for AI " + aiid);
            AiBot bot = this.database.getPublishedBotForAI(devId, aiid);
            return bot != null ? new ApiAiBot(bot).setSuccessStatus() : ApiError.getNotFound();
        } catch (Database.DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    private void stopTrainingIfNeeded(final String devId, final UUID aiid)
            throws Database.DatabaseException {
        try {
            ApiAi ai = this.database.getAI(devId, aiid, this.jsonSerializer);
            TrainingStatus status = ai.getSummaryAiStatus();
            if (status == TrainingStatus.AI_TRAINING || status == TrainingStatus.AI_TRAINING_QUEUED) {
                this.aiServices.stopTraining(devId, aiid);
            }
        } catch (ServerConnector.AiServicesException ex) {
            this.logger.logWarning(LOGFROM, "Could not stop training for ai " + aiid);
        }
    }
}
