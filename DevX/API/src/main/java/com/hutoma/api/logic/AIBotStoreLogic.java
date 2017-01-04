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
            this.logger.logDebug(LOGFROM, "request to get all published bots");
            return new ApiAiBotList(this.database.getPublishedBots()).setSuccessStatus();
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getPurchasedBots(final String devId) {
        try {
            this.logger.logDebug(LOGFROM, "request purchased bots for devId " + devId);
            return new ApiAiBotList(this.database.getPurchasedBots(devId)).setSuccessStatus();
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult purchaseBot(final String devId, final int botId) {
        try {
            this.logger.logDebug(LOGFROM, String.format("request to purchase bot %d for devId %s", botId, devId));
            // Check if the bot has already been purchased
            List<AiBot> alreadyPurchased = this.database.getPurchasedBots(devId);
            if (alreadyPurchased.stream().anyMatch(x -> x.getBotId() == botId)) {
                this.logger.logInfo(LOGFROM, String.format(
                        "Could not purchase bot %d for devId %s since it's already owned", botId, devId));
                return ApiError.getBadRequest("Bot already purchased");
            }
            if (this.database.purchaseBot(devId, botId)) {
                this.logger.logInfo(LOGFROM,
                        String.format("Purchase of bot %d for devId %s was successful", botId, devId));
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logWarning(LOGFROM,
                        String.format("Could not purchase bot %d for devId %s", botId, devId));
                return ApiError.getNotFound("Bot not found");
            }
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }
}