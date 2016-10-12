package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiIntegrations;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiIntegration;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

/**
 * Created by Andrea on 30/09/16.
 */
public class AIIntegrationLogic {

    private static final String LOGFROM = "aiintegrationlogic";
    private Config config;
    private JsonSerializer jsonSerializer;
    private Database database;
    private Logger logger;

    @Inject
    public AIIntegrationLogic(Config config, JsonSerializer jsonSerializer, Database database, Logger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
    }

    public ApiResult getIntegrations(
        SecurityContext securityContext) {

        try {
            this.logger.logDebug(LOGFROM, "request to get all integration ");
            List<AiIntegration> integrationList = this.database.getAiIntegrationList();
            if (integrationList.size() == 0) {
                this.logger.logDebug(LOGFROM, "no integration found");
                return ApiError.getNotFound();
            }
            return new ApiAiIntegrations(integrationList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "could not get all integration; " + e.toString());
            return ApiError.getInternalServerError();
        }
    }
}