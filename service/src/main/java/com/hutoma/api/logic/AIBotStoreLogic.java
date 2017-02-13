package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiString;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.DeveloperInfo;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;

/**
 * AI Bot Store logic.
 */
public class AIBotStoreLogic {

    public static final long MAX_ICON_FILE_SIZE = 512 * 1024; // 512Kb
    private static final String LOGFROM = "botstorelogic";
    private static final Set<String> ALLOWED_ICON_EXT = new HashSet<>(Arrays.asList("png", "jpg", "jpeg"));
    private final Database database;
    private final ILogger logger;
    private final Config config;
    private final JsonSerializer jsonSerializer;

    @Inject
    public AIBotStoreLogic(Database database, ILogger logger, final Config config,
                           final JsonSerializer jsonSerializer) {
        this.database = database;
        this.logger = logger;
        this.config = config;
        this.jsonSerializer = jsonSerializer;
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
            ApiAi ai = this.database.getAI(devId, aiid, this.jsonSerializer);
            if (ai.getSummaryAiStatus() != TrainingStatus.AI_TRAINING_COMPLETE) {
                return ApiError.getBadRequest("AI needs to be fully trained before being allowed to be published");
            }
            List<AiBot> linkedBots = this.database.getBotsLinkedToAi(devId, aiid);
            if (!linkedBots.isEmpty()) {
                return ApiError.getBadRequest(
                        "Publishing an AI that is already linked to one or more bots is not supported");
            }
            bot = new AiBot(devId, aiid, -1, name, description, longDescription, alertMessage, badge, price,
                    sample, category, licenseType, DateTime.now(), privacyPolicy, classification, version,
                    videoLink, true, null);
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
            String iconPath = this.database.getBotIconPath(botId);
            if (iconPath != null) {
                return new ApiString(iconPath).setSuccessStatus();
            } else {
                return ApiError.getNotFound();
            }
        } catch (Database.DatabaseException e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult uploadBotIcon(final String devId, final int botId, final InputStream inputStream,
                                   final FormDataContentDisposition fileDetail) {
        File tempFile = null;
        try {
            AiBot bot = this.database.getBotDetails(botId);
            if (bot == null || !bot.getDevId().equals(devId)) {
                this.logger.logInfo(LOGFROM,
                        String.format("Request for uploading image for bot %d from devid %s who is not the owner",
                                botId, devId));
                return ApiError.getNotFound();
            }
            String extension = FilenameUtils.getExtension(fileDetail.getFileName()).toLowerCase();
            if (!ALLOWED_ICON_EXT.contains(extension)) {
                this.logger.logInfo(LOGFROM, String.format(
                        "Request to upload image with not allowed extension (%s) for bot %d",
                        extension, botId));
                return ApiError.getBadRequest("Image extension not allowed");
            }

            // Start saving the stream to a temporary file
            tempFile = File.createTempFile("boticon", ".tmp");
            final int bufferLen = 1024;
            // Save until the stream ends or reaches the maximum allowed size
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[bufferLen];
                int n1;
                for (long count = 0L; -1 != (n1 = inputStream.read(buffer)); count += (long) n1) {
                    out.write(buffer, 0, n1);
                    if ((count + n1) > MAX_ICON_FILE_SIZE) {
                        return ApiError.getBadRequest(
                                String.format("File is larger than the maximum allowed size (%d bytes)",
                                        MAX_ICON_FILE_SIZE));
                    }
                }
            }

            // Copy the temp file into it's final destination
            String destFilename = String.format("%d.%s", botId, extension);
            File destFile = new File(this.config.getBotIconStoragePath(), destFilename);
            Files.copy(tempFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            this.database.saveBotIconPath(devId, botId, destFilename);
            return new ApiResult().setSuccessStatus();

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