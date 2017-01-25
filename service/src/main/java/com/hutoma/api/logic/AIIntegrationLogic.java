package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntegrationList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.Integration;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by Andrea on 30/09/16.
 */
public class AIIntegrationLogic {

    private static final String LOGFROM = "aiintegrationlogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final ILogger logger;

    @Inject
    public AIIntegrationLogic(Config config, JsonSerializer jsonSerializer, Database database, ILogger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
    }

    public ApiResult getIntegrations(
            SecurityContext securityContext) {

        try {
            List<Integration> integrationList = this.database.getAiIntegrationList();
            if (integrationList.size() == 0) {
                this.logger.logDebug(LOGFROM, "no integrations found");
                return ApiError.getNotFound();
            }
            return new ApiIntegrationList(integrationList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }
}