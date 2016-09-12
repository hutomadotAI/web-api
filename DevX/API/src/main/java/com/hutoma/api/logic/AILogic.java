package com.hutoma.api.logic;

import com.hutoma.api.auth.Role;
import com.hutoma.api.common.*;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.MessageQueue;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.validation.Validate;
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

    Config config;
    JsonSerializer jsonSerializer;
    Database database;
    MessageQueue messageQueue;
    Logger logger;
    Tools tools;

    private final String LOGFROM = "ailogic";

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
            int status)
    {
        try {
            logger.logDebug(LOGFROM, "request to create new ai from " + devid);

            String encoding_key = config.getEncodingKey();
            UUID aiUUID = tools.createNewRandomUUID();

            String token = Jwts.builder()
                    .claim("ROLE", Role.ROLE_CLIENTONLY)
                    .claim("AIID", aiUUID)
                    .setSubject(devid)
                    .compressWith(CompressionCodecs.DEFLATE)
                    .signWith(SignatureAlgorithm.HS256, encoding_key)
                    .compact();

            if (!database.createAI(aiUUID, name, description, devid, is_private,
                    deep_learning_error, deep_learning_status,
                    shallow_learning_status, status, token, "")) {
                logger.logInfo(LOGFROM, "db fail creating new ai");
                return ApiError.getInternalServerError();
            }
            return new ApiAi(aiUUID.toString(), token).setSuccessStatus("successfully created");
        }
        catch (Exception e){
            logger.logError(LOGFROM, "error creating new ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getAIs(
            SecurityContext securityContext,
            String devid) {

       try {
           logger.logDebug(LOGFROM, "request to list all ais");
           ArrayList<ApiAi> aiList = database.getAllAIs(devid);
           if (aiList.isEmpty()) {
               logger.logDebug(LOGFROM, "ai list is empty");
               return ApiError.getNotFound();
           }
           return new ApiAiList(aiList).setSuccessStatus();
       }
       catch (Exception e){
           logger.logError(LOGFROM, "error getting all ais: " + e.toString());
           return ApiError.getInternalServerError();
       }
    }

    public ApiResult getSingleAI(
            SecurityContext securityContext,
            String devid,
            UUID aiid) {

        try {
            logger.logDebug(LOGFROM, devid + " request to list " + aiid);
            ApiAi ai = database.getAI(aiid);
            if (null == ai) {
                logger.logDebug(LOGFROM, "ai not found");
                return ApiError.getNotFound();
            } else {
                return ai.setSuccessStatus();
            }
        }
        catch (Exception e){
            logger.logError(LOGFROM, "error getting single ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteAI(
            SecurityContext securityContext,
            String devid,
            UUID aiid) {

        try {
            logger.logDebug(LOGFROM, devid + " request to delete " + aiid);
            if(!database.deleteAi(aiid))
            {
                return ApiError.getNotFound();
            }
            messageQueue.pushMessageDeleteAI(devid, aiid);
            return new ApiResult().setSuccessStatus("deleted successfully");
        }
        catch (Exception e) {
            logger.logError(LOGFROM, "error deleting ai: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

}
