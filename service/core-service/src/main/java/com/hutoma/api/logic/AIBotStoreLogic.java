package com.hutoma.api.logic;

import com.hutoma.api.common.BotStructureSerializer;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.*;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.DeveloperInfo;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.nio.file.attribute.PosixFilePermission.*;

/**
 * AI Bot Store logic.
 */
public class AIBotStoreLogic {

    static final long MAX_ICON_FILE_SIZE = 512 * 1024; // 512Kb
    private static final String LOGFROM = "botstorelogic";
    private static final Set<String> ALLOWED_ICON_EXT = new HashSet<>(Arrays.asList("png", "jpg", "jpeg"));
    private final DatabaseAI database;
    private final DatabaseEntitiesIntents databaseEntitiesIntents;
    private final DatabaseMarketplace databaseMarketplace;
    private final ILogger logger;
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Provider<DatabaseTransaction> databaseTransactionProvider;

    @Inject
    AIBotStoreLogic(final DatabaseAI database, final DatabaseEntitiesIntents databaseEntitiesIntents,
                    final DatabaseMarketplace databaseMarketplace,
                    final Provider<DatabaseTransaction> databaseTransactionProvider,
                    final ILogger logger, final Config config, final JsonSerializer jsonSerializer) {
        this.database = database;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.databaseMarketplace = databaseMarketplace;
        this.databaseTransactionProvider = databaseTransactionProvider;
        this.logger = logger;
        this.config = config;
        this.jsonSerializer = jsonSerializer;
    }

    public ApiResult getPublishedBots(final UUID devId) {
        return getPublishedSkills(devId);
    }

    public ApiResult getPublishedSkills(final UUID devId) {
        return getPublishedBotByType(devId, AiBot.PublishingType.SKILL);
    }

    public ApiResult getPublishedTemplates(final UUID devId) {
        return getPublishedBotByType(devId, AiBot.PublishingType.TEMPLATE);
    }

    private ApiResult getPublishedBotByType(final UUID devId, final AiBot.PublishingType publishingType) {
        try {
            List<AiBot> bots = this.databaseMarketplace.getPublishedBots(publishingType);
            this.logger.logUserTraceEvent(LOGFROM, "GetPublishedBots", devId.toString(),
                    LogMap.map("Num Bots", bots.size()));
            return new ApiAiBotList(bots).setSuccessStatus();
        } catch (DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetPublishedBots", devId.toString(), e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getPurchasedBots(final UUID devId) {
        try {
            List<AiBot> bots = this.databaseMarketplace.getPurchasedBots(devId);
            this.logger.logUserTraceEvent(LOGFROM, "GetPurchasedBots", devId.toString(),
                    LogMap.map("Num Bots", bots.size()));
            return new ApiAiBotList(bots).setSuccessStatus();
        } catch (DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetPurchasedBots", devId.toString(), e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult purchaseBot(final UUID devId, final int botId) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("BotId", botId);
            // Check if the bot actually exists in the store
            AiBot bot = this.databaseMarketplace.getBotDetails(botId);
            if (bot == null || bot.getPublishingState() != AiBot.PublishingState.PUBLISHED) {
                this.logger.logUserTraceEvent(LOGFROM, String.format("Bot not %s",
                        bot == null ? "found" : "published"), devIdString, logMap);
                return ApiError.getNotFound("Bot not found");
            }
            // Check if the bot has already been purchased
            List<AiBot> alreadyPurchased = this.databaseMarketplace.getPurchasedBots(devId);
            if (alreadyPurchased.stream().anyMatch(x -> x.getBotId() == botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "PurchaseBot - attempt purchase already owned bot", devIdString,
                        logMap);
                return ApiError.getBadRequest("Bot already purchased");
            }
            if (this.databaseMarketplace.purchaseBot(devId, botId)) {
                this.logger.logUserTraceEvent(LOGFROM, "PurchaseBot", devIdString, logMap);
                return new ApiResult().setSuccessStatus();
            } else {
                this.logger.logUserErrorEvent(LOGFROM, "PurchaseBot - could not purchase", devIdString, logMap);
                return ApiError.getInternalServerError();
            }
        } catch (DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "PurchaseBot", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getBotDetails(final UUID devId, final int botId) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("BotId", botId);
            AiBot bot = this.databaseMarketplace.getBotDetails(botId);
            if (bot == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetBotDetails - not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetBotDetails", devIdString, logMap);
            return new ApiAiBot(bot).setSuccessStatus();
        } catch (Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetBotDetails", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getBotTemplate(final UUID devId, final int botId) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("BotId", botId);
            AiBot bot = this.databaseMarketplace.getBotDetails(botId);
            if (bot == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetBotTemplate - bot not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            if (bot.getPublishingType() != AiBot.PublishingType.TEMPLATE) {
                this.logger.logUserTraceEvent(LOGFROM, "GetBotTemplate - not a template", devIdString, logMap);
                return ApiError.getBadRequest("Bot is not a template");
            }
            String template = this.databaseMarketplace.getBotTemplate(botId);
            if (template == null) {
                this.logger.logUserTraceEvent(LOGFROM, "GetBotTemplate - template not found", devIdString, logMap);
                return ApiError.getNotFound();
            }
            BotStructure botStructure = (BotStructure) this.jsonSerializer.deserialize(template, BotStructure.class);
            this.logger.logUserTraceEvent(LOGFROM, "GetBotTemplate", devIdString, logMap);
            return new ApiBotStructure(botStructure).setSuccessStatus();
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetBotTemplate", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult publishBot(final UUID devId, final UUID aiid, final String name, final String description,
                                final String longDescription, final String alertMessage, final String badge,
                                final BigDecimal price, final String sample, final String category,
                                final String licenseType, final String privacyPolicy, final String classification,
                                final String version, final String videoLink,
                                final AiBot.PublishingType publishingType) {
        final String devIdString = devId.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            DeveloperInfo devInfo = this.databaseMarketplace.getDeveloperInfo(devId);
            if (devInfo == null) {
                this.logger.logUserTraceEvent(LOGFROM, "PublishBot - DevInfo not entered", devIdString, logMap);
                return ApiError.getBadRequest("Please set the developer information first");
            }
            AiBot bot = this.databaseMarketplace.getPublishedBotForAI(devId, aiid);
            if (bot != null) {
                this.logger.logUserTraceEvent(LOGFROM, "PublishBot - AI already has published bot",
                        devIdString, logMap);
                return ApiError.getBadRequest("Bot already has a published bot");
            }
            if (publishingType == AiBot.PublishingType.SKILL) {
                List<AiBot> linkedBots = this.database.getBotsLinkedToAi(devId, aiid);
                if (!linkedBots.isEmpty()) {
                    this.logger.logUserTraceEvent(LOGFROM,
                            "PublishBot - Attempting to publish a skill with linked bots", devIdString, logMap);
                    return ApiError.getBadRequest("Bot cannot be published as a skill when it has linked bots");
                }
                ApiAi ai = this.database.getAI(devId, aiid, this.jsonSerializer);
                if (ai.getSummaryAiStatus() != TrainingStatus.AI_TRAINING_COMPLETE) {
                    this.logger.logUserTraceEvent(LOGFROM, "PublishBot - AI not trained", devIdString, logMap);
                    return ApiError.getBadRequest("Bot needs to be fully trained before being allowed to be published");
                }
            }
            bot = new AiBot(devId, aiid, -1, name, description, longDescription, alertMessage, badge, price,
                    sample, category, licenseType, DateTime.now(), privacyPolicy, classification, version,
                    videoLink, AiBot.PublishingState.SUBMITTED, publishingType, null);

            int botId;
            if (publishingType == AiBot.PublishingType.TEMPLATE) {
                try (DatabaseTransaction transaction = this.databaseTransactionProvider.get()) {
                    botId = this.databaseMarketplace.publishBot(bot, transaction);
                    if (botId == -1) {
                        transaction.rollback();
                        this.logger.logUserTraceEvent(LOGFROM, "PublishBot - invalid request", devIdString, logMap);
                        return ApiError.getBadRequest("Invalid publish request");
                    }
                    BotStructure botStructure = BotStructureSerializer.serialize(devId, aiid, this.database,
                            this.databaseEntitiesIntents, this.jsonSerializer);
                    if (!this.databaseMarketplace.addBotTemplate(botId, botStructure, transaction,
                            this.jsonSerializer)) {
                        transaction.rollback();
                        this.logger.logUserTraceEvent(LOGFROM, "PublishBot - could not write the template",
                                devIdString, logMap);
                        return ApiError.getInternalServerError();
                    } else {
                        transaction.commit();
                    }
                }
            } else {
                botId = this.databaseMarketplace.publishBot(bot, null);
                if (botId == -1) {
                    this.logger.logUserTraceEvent(LOGFROM, "PublishBot - invalid request", devIdString, logMap);
                    return ApiError.getBadRequest("Invalid publish request");
                }
            }

            bot.setBotId(botId);
            this.logger.logUserTraceEvent(LOGFROM, "PublishBot", devIdString, logMap.put("New BotId", botId));
            return new ApiAiBot(bot).setSuccessStatus();
        } catch (DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "PublishBot", devIdString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getBotIcon(final int botId) {
        try {
            String iconPath = this.databaseMarketplace.getBotIconPath(botId);
            if (iconPath != null) {
                return new ApiString(iconPath).setSuccessStatus();
            } else {
                return ApiError.getNotFound();
            }
        } catch (DatabaseException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetBotIcon", "", e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult uploadBotIcon(final UUID devId, final int botId, final InputStream inputStream,
                                   final FormDataContentDisposition fileDetail) {
        final String devIdString = devId.toString();
        File tempFile = null;
        try {
            LogMap logMap = LogMap.map("BotId", botId);
            AiBot bot = this.databaseMarketplace.getBotDetails(botId);
            if (bot == null || !bot.getDevId().equals(devId)) {
                this.logger.logUserTraceEvent(LOGFROM, "UploadBotIcon - request uploading for other dev's bot",
                        devIdString, logMap);
                return ApiError.getNotFound();
            }
            String extension = FilenameUtils.getExtension(fileDetail.getFileName()).toLowerCase();
            if (!ALLOWED_ICON_EXT.contains(extension)) {
                this.logger.logUserTraceEvent(LOGFROM, "UploadBotIcon - invalid extension", devIdString,
                        logMap.put("Extension", extension));
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
                        this.logger.logUserTraceEvent(LOGFROM, "UploadBotIcon - exceeded max size", devIdString,
                                logMap);
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
            // Make sure the file permissions are set to ALL-READ
            Files.setPosixFilePermissions(destFile.toPath(),
                    EnumSet.of(OWNER_WRITE, OWNER_READ, GROUP_READ, OTHERS_READ));

            this.databaseMarketplace.saveBotIconPath(devId, botId, destFilename);
            this.logger.logUserTraceEvent(LOGFROM, "UploadBotIcon", devIdString, logMap);
            return new ApiResult().setSuccessStatus();

        } catch (DatabaseException | IOException e) {
            this.logger.logUserExceptionEvent(LOGFROM, "UploadBotIcon", devIdString, e);
            return ApiError.getInternalServerError();
        } finally {
            if (tempFile != null) {
                if (!tempFile.delete()) {
                    this.logger.logUserWarnEvent(LOGFROM, "UploadBotIcon - could not delete temp file",
                            devIdString, LogMap.map("Path", tempFile.getAbsolutePath()));
                }
            }
        }
    }
}
