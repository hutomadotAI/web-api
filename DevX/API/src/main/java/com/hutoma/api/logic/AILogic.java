package com.hutoma.api.logic;

import com.hutoma.api.access.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;


/**
 * Created by mauriziocibelli on 27/04/16.
 */
public class AILogic {

    private static final double DEEP_LEARNING_ERROR = -1.0;
    private static final int DEEP_LEARNING_STATUS = -1;
    private static final int DEFAULT_WNET_ERROR = -1;
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

            if (!this.database.createAI(
                    aiUUID,
                    name,
                    description,
                    devid,
                    isPrivate,
                    DEEP_LEARNING_ERROR,
                    DEEP_LEARNING_STATUS,
                    DEFAULT_WNET_ERROR,
                    TrainingStatus.NOT_STARTED,
                    token,
                    null,
                    language,
                    timezone,
                    confidence,
                    personality,
                    voice)) {
                this.logger.logInfo(LOGFROM, "db fail creating new ai");
                return ApiError.getInternalServerError();
            }
            return new ApiAi(aiUUID.toString(), token).setSuccessStatus("successfully created");
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "error creating new ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateAIStatus(final SecurityContext securityContext, final AiStatus status) {
        try {
            if (!this.database.updateAIStatus(
                    status.getDevId(),
                    status.getAiid(),
                    status.getTrainingStatus())) {
                return ApiError.getInternalServerError("Could not update");
            }
            return new ApiResult().setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            return ApiError.getInternalServerError(ex.getMessage());
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
            this.logger.logError(LOGFROM, "error updating ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getAIs(
            SecurityContext securityContext,
            String devid) {

        try {
            this.logger.logDebug(LOGFROM, "request to list all ais");
            List<ApiAi> aiList = this.database.getAllAIs(devid);
            if (aiList.isEmpty()) {
                this.logger.logDebug(LOGFROM, "ai list is empty");
                return ApiError.getNotFound();
            }
            return new ApiAiList(aiList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "error getting all ais: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getSingleAI(
            SecurityContext securityContext,
            String devid,
            UUID aiid) {

        try {
            this.logger.logDebug(LOGFROM, devid + " request to list " + aiid);
            ApiAi ai = this.database.getAI(devid, aiid);
            if (null == ai) {
                this.logger.logDebug(LOGFROM, "ai not found");
                return ApiError.getNotFound();
            } else {
                return ai.setSuccessStatus();
            }
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "error getting single ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteAI(
            SecurityContext securityContext,
            String devid,
            UUID aiid) {

        try {
            this.logger.logDebug(LOGFROM, devid + " request to delete " + aiid);
            if (!this.database.deleteAi(devid, aiid)) {
                return ApiError.getNotFound();
            }
            this.aiServices.deleteAI(devid, aiid);
            return new ApiResult().setSuccessStatus("deleted successfully");
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "error deleting ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

}
