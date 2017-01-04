package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
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

    public ApiResult getBotDetails(final int botId) {
        try {
            this.logger.logDebug(LOGFROM, "request to get details of bot " + botId);
            AiBot bot = this.database.getBotDetails(botId);
            return bot == null ? ApiError.getNotFound() : new ApiAiBot(bot).setSuccessStatus();
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult publishBot(final String devId, final UUID aiid, final String name, final String description,
                                final String longDescription, final String alertMessage, final String badge,
                                final BigDecimal price, final String sample, final String category,
                                final String privacyPolicy, final String classification, final String version,
                                final String videoLink) {
        try {
            this.logger.logDebug(LOGFROM, "request to publish bot for AI " + aiid.toString());
            AiBot bot = new AiBot(devId, aiid, -1, name, description, longDescription, alertMessage, badge, price,
                    sample, category, DateTime.now(), privacyPolicy, classification, version, videoLink, true);
            int botId = this.database.publishBot(bot);
            if (botId == -1) {
                return ApiError.getBadRequest("Invalid publish request");
            } else {
                bot.setBotId(botId);
                return new ApiAiBot(bot).setSuccessStatus();
            }
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }
}