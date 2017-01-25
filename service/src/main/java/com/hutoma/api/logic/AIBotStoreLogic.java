package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiStreamResult;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.DeveloperInfo;

import org.apache.commons.compress.utils.IOUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

/**
 * AI Bot Store logic.
 */
public class AIBotStoreLogic {

    public static final long MAX_ICON_FILE_SIZE = 512 * 1024; // 512Kb
    private static final String LOGFROM = "botstorelogic";
    private final Database database;
    private final ILogger logger;
    private final Tools tools;

    @Inject
    public AIBotStoreLogic(Database database, ILogger logger, final Tools tools) {
        this.database = database;
        this.logger = logger;
        this.tools = tools;
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
            // Check if the bot actually exists in the store
            AiBot bot = this.database.getBotDetails(botId);
            if (bot == null || !bot.isPublished()) {
                this.logger.logInfo(LOGFROM, String.format("Bot %d not %s", botId,
                        bot == null ? "found" : "published"));
                return ApiError.getNotFound("Bot not found");
            }
            if (bot.getDevId().equals(devId)) {
                this.logger.logInfo(LOGFROM, String.format("Dev %s attempted to purchase own bot", botId));
                return ApiError.getBadRequest("Cannot purchase own bot");
            }
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
                return ApiError.getInternalServerError();
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
                                final String licenseType, final String privacyPolicy, final String classification,
                                final String version, final String videoLink) {
        try {
            this.logger.logDebug(LOGFROM, "request to publish bot for AI " + aiid.toString());
            DeveloperInfo devInfo = this.database.getDeveloperInfo(devId);
            if (devInfo == null) {
                return ApiError.getBadRequest("Developer information hasn't been update yet");
            }
            AiBot bot = this.database.getPublishedBotForAI(devId, aiid);
            if (bot != null) {
                return ApiError.getBadRequest("AI already has a published bot");
            }
            bot = new AiBot(devId, aiid, -1, name, description, longDescription, alertMessage, badge, price,
                    sample, category, licenseType, DateTime.now(), privacyPolicy, classification, version,
                    videoLink, true);
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

    public ApiResult getBotIcon(final int botId) {
        try {
            InputStream inputStream = this.database.getBotIcon(botId);
            if (inputStream == null) {
                return ApiError.getNotFound();
            } else {
                return new ApiStreamResult((outStream) -> IOUtils.copy(inputStream, outStream)).setSuccessStatus();
            }
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult uploadBotIcon(final String devId, final int botId, final InputStream inputStream) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("boticon", ".tmp");
            final int bufferLen = 1024;
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[bufferLen];
                int n1;
                for (long count = 0L; -1 != (n1 = inputStream.read(buffer)); count += (long) n1) {
                    out.write(buffer, 0, n1);
                    if ((count + n1) > MAX_ICON_FILE_SIZE) {
                        return ApiError.getBadRequest(
                                String.format("File is larger than the maximum allowed size (%d bytes)", MAX_ICON_FILE_SIZE));
                    }
                }
            }

            InputStream inputStreamTempFile = new FileInputStream(tempFile);
            if (this.database.saveBotIcon(devId, botId, inputStreamTempFile)) {
                return new ApiResult().setSuccessStatus();
            } else {
                return ApiError.getNotFound();
            }
        } catch (Database.DatabaseException | IOException e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        } finally {
            if (tempFile != null) {
                if (!tempFile.delete()) {
                    this.logger.logWarning(LOGFROM, "Could not delete temp file " + tempFile.getAbsolutePath());
                }
            }
        }
    }
}