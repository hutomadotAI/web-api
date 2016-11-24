package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiStore;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStore;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by Hutoma on 15/07/16.
 */
public class AIBotStoreLogic {

    private static final String LOGFROM = "aidomainlogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final ILogger logger;

    @Inject
    public AIBotStoreLogic(Config config, JsonSerializer jsonSerializer, Database database, ILogger logger) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.logger = logger;
    }

    public ApiResult getBots(
            SecurityContext securityContext) {

        try {
            this.logger.logDebug(LOGFROM, "request to get all domains ");
            List<AiStore> domainList = this.database.getBotStoreList();
            if (domainList.size() == 0) {
                this.logger.logDebug(LOGFROM, "no domains found");
                return ApiError.getNotFound();
            }
            return new ApiAiStore(domainList).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logError(LOGFROM, "could not get all domains; " + e.toString());
            return ApiError.getInternalServerError();
        }
    }
}