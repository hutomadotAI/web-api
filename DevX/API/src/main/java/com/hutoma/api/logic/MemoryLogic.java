package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.NeuralNet;
import com.hutoma.api.connectors.SemanticAnalysis;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiMemory;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiMemoryToken;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

public class MemoryLogic {

    Config config;
    Database database;
    Tools tools;
    Logger logger;

    private final String LOGFROM = "memorylogic";

    @Inject
    public MemoryLogic(Config config, Database database, Tools tools, Logger logger) {
        this.config = config;
        this.database = database;
        this.tools = tools;
        this.logger = logger;
    }

    public ApiResult getVariables(SecurityContext securityContext,
                                  String dev_id,
                                  String aiid,
                                  String uid) {
        try {
            logger.logDebug(LOGFROM, "request to load all user variables for " + dev_id);
            // load everything
            List<ApiMemoryToken> resultList = database.getAllUserVariables(dev_id, aiid, uid);

            // if the list is empty then throw a 404
            if (resultList.isEmpty()) {
                logger.logDebug(LOGFROM, "no variables found");
                return ApiError.getNotFound("user variable not found");
            }
            return new ApiMemory(resultList).setSuccessStatus();
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to get memory variables " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getSingleVariable (SecurityContext securityContext,
                                String dev_id,
                                String aiid,
                                String uid,
                                String variable) {
        try {
            logger.logDebug(LOGFROM, "request to load single user variable for " + dev_id);
            ApiMemoryToken token = database.getUserVariable(dev_id, aiid, uid, variable);

            // if the token is null then throw a 404
            if (null==token) {
                logger.logDebug(LOGFROM, "variable not found");
                return ApiError.getNotFound("user variable not found");
            }
            return token.setSuccessStatus();
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to get memory variable " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult setVariable(SecurityContext securityContext,
                                String dev_id,
                                String aiid,
                                String uid,
                                String variable,
                                String value,
                                int n_prompts,
                                int expires_seconds,
                                String label
                                ) {
        try {
            logger.logDebug(LOGFROM, "request to set user variable for " + dev_id);
            boolean success = database.setUserVariable(dev_id, aiid, uid, expires_seconds, n_prompts, label, variable, value);
            if (!success) {
                logger.logInfo(LOGFROM, "zero rows changed on attempt to set variable");
                return ApiError.getBadRequest("could not set variable");
            }
            return new ApiResult().setSuccessStatus("variable set");
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to set memory variable " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult delVariable(SecurityContext securityContext,
                                String dev_id,
                                String aiid,
                                String uid,
                                String variable ) {
        try {
            logger.logDebug(LOGFROM, "request to delete user variable for " + dev_id);
            boolean deleted = database.removeVariable(dev_id, aiid, uid, variable);
            if (!deleted) {
                logger.logDebug(LOGFROM, "variable not found");
                return ApiError.getNotFound("variable not found");
            }
            return new ApiResult().setSuccessStatus("deleted");
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to delete memory variable " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult removeAllUserVariables(SecurityContext securityContext,
                                                 String dev_id,
                                                 String aiid,
                                                 String uid) {
        try {
            logger.logDebug(LOGFROM, "request to delete all user variables " + dev_id);
            boolean deleted = database.removeAllUserVariables(dev_id, aiid, uid);
            if (!deleted) {
                logger.logDebug(LOGFROM, "variables not found");
                return ApiError.getNotFound("variables not found");
            }
            return new ApiResult().setSuccessStatus("deleted");
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to delete user memory variables " + e.toString());
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult removeAllAiVariables(SecurityContext securityContext,
                                            String dev_id,
                                            String aiid) {
        try {
            logger.logDebug(LOGFROM, "request to delete all ai variables " + dev_id);
            boolean deleted = database.removeAllAiVariables(dev_id, aiid);
            if (!deleted) {
                logger.logDebug(LOGFROM, "variables not found");
                return ApiError.getNotFound("variables not found");
            }
            return new ApiResult().setSuccessStatus("deleted");
        } catch (Exception e) {
            logger.logError(LOGFROM, "failed to delete ai memory variables " + e.toString());
            return ApiError.getInternalServerError();
        }
    }
}
