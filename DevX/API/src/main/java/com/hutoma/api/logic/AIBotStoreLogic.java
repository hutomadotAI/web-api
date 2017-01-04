package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;

import java.util.List;
import javax.inject.Inject;

/**
 * AI Bot Store logic.
 */
public class AIBotStoreLogic {

    private static final String LOGFROM = "botstorelogic";
    private final Database database;
    private final ILogger logger;

    @Inject
    public AIBotStoreLogic(Database database, ILogger logger) {
        this.database = database;
        this.logger = logger;
    }

    public ApiResult getPublishedBots() {
        try {
            this.logger.logDebug(LOGFROM, "request to get all domains ");
            List<AiBot> bots = this.database.getPublishedBots();
            if (bots.size() == 0) {
                this.logger.logDebug(LOGFROM, "no bots found");
                return ApiError.getNotFound();
            }
            return new ApiAiBotList(bots).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }
}