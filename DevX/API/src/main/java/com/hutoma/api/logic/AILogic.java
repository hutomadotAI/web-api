package com.hutoma.api.logic;

import com.hutoma.api.access.Role;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.TrainingStatus;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by mauriziocibelli on 27/04/16.
 */
public class AILogic {

    private final String LOGFROM = "ailogic";
    private final double DEEP_LEARNING_ERROR = -1.0;
    private final int DEEP_LEARNING_STATUS = -1;
    private final int DEFAULT_WNET_ERROR = -1;
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final MessageQueue messageQueue;
    private final Logger logger;
    private final Tools tools;

    @Inject
    public AILogic(Config config, JsonSerializer jsonSerializer, Database database, MessageQueue messageQueue,
                   Logger logger, Tools tools) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.messageQueue = messageQueue;
        this.logger = logger;
        this.tools = tools;
    }

    public ApiResult createAI(
            SecurityContext securityContext,
            String devid,
            String name,
            String description,
            boolean is_private,
            double deep_learning_error,
            int deep_learning_status,
            int shallow_learning_status,
            int status) {
        try {
            this.logger.logDebug(this.LOGFROM, "request to create new ai from " + devid);

            String encoding_key = this.config.getEncodingKey();
            UUID aiUUID = this.tools.createNewRandomUUID();

            String token = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .claim("AIID", aiUUID)
                    .setSubject(devid)
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encoding_key)
                    .compact();

            if (!this.database.createAI(aiUUID, name, description, devid, is_private,
                    this.DEEP_LEARNING_ERROR, this.DEEP_LEARNING_STATUS, this.DEFAULT_WNET_ERROR, TrainingStatus.NOT_STARTED, token, "")) {
                this.logger.logInfo(this.LOGFROM, "db fail creating new ai");
                return ApiError.getInternalServerError();
            }
            return new ApiAi(aiUUID.toString(), token).setSuccessStatus("successfully created");
        } catch (Exception e) {
            this.logger.logError(this.LOGFROM, "error creating new ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getAIs(
            SecurityContext securityContext,
            String devid) {

        try {
            this.logger.logDebug(this.LOGFROM, "request to list all ais");
            ArrayList<ApiAi> aiList = this.database.getAllAIs(devid);
            if (aiList.isEmpty()) {
                this.logger.logDebug(this.LOGFROM, "ai list is empty");
                return ApiError.getNotFound();
            }
            return new ApiAiList(aiList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logError(this.LOGFROM, "error getting all ais: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getSingleAI(
            SecurityContext securityContext,
            String devid,
            UUID aiid) {

        try {
            this.logger.logDebug(this.LOGFROM, devid + " request to list " + aiid);
            ApiAi ai = this.database.getAI(devid, aiid);
            if (null == ai) {
                this.logger.logDebug(this.LOGFROM, "ai not found");
                return ApiError.getNotFound();
            } else {
                return ai.setSuccessStatus();
            }
        } catch (Exception e) {
            this.logger.logError(this.LOGFROM, "error getting single ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteAI(
            SecurityContext securityContext,
            String devid,
            UUID aiid) {

        try {
            this.logger.logDebug(this.LOGFROM, devid + " request to delete " + aiid);
            if (!this.database.deleteAi(devid, aiid)) {
                return ApiError.getNotFound();
            }
            this.messageQueue.pushMessageDeleteAI(devid, aiid);
            return new ApiResult().setSuccessStatus("deleted successfully");
        } catch (Exception e) {
            this.logger.logError(this.LOGFROM, "error deleting ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

}
