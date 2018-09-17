package com.hutoma.api.logic;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.DeveloperInfoHelper;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestBotHelper;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseMarketplace;
import com.hutoma.api.connectors.db.DatabaseTransaction;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiBotStructure;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiString;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.BotStructure;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;

import org.apache.commons.lang.SystemUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import javax.inject.Provider;

import static com.hutoma.api.common.TestBotHelper.*;
import static com.hutoma.api.common.TestDataHelper.DEVID_UUID;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AI Bot Store logic.
 */
public class TestAIBotstoreLogic {

    private static final UUID ALTERNATE_DEVID = UUID.randomUUID();
    private static final String BOT_ICON_PATH = BOTID + ".png";
    private final ByteArrayInputStream botIconStream = new ByteArrayInputStream(TestBotHelper.getBotIconContent());
    private DatabaseAI fakeDatabaseAi;
    private DatabaseMarketplace fakeDatabaseMarketplace;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private AIBotStoreLogic aiBotStoreLogic;
    private Config fakeConfig;
    private FormDataContentDisposition iconContentDisp;
    private Provider<DatabaseTransaction> fakeDatabaseTransactionProvider;
    private DatabaseTransaction fakeDatabaseTransaction;
    private JsonSerializer fakeJsonSerializer;

    @Before
    public void setup() {
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseMarketplace = mock(DatabaseMarketplace.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabaseTransactionProvider = mock(Provider.class);
        this.fakeDatabaseTransaction = mock(DatabaseTransaction.class);
        this.fakeJsonSerializer = mock(JsonSerializer.class);
        this.aiBotStoreLogic = new AIBotStoreLogic(this.fakeDatabaseAi, this.fakeDatabaseEntitiesIntents,
                this.fakeDatabaseMarketplace, this.fakeDatabaseTransactionProvider, mock(ILogger.class),
                this.fakeConfig, this.fakeJsonSerializer);
        this.iconContentDisp = FormDataContentDisposition.name("file").fileName(BOT_ICON_PATH).build();
        // Store any bot icons in the temp folder
        when(this.fakeConfig.getBotIconStoragePath()).thenReturn(System.getProperty("java.io.tmpdir"));
        when(this.fakeDatabaseTransactionProvider.get()).thenReturn(this.fakeDatabaseTransaction);
    }

    @Test
    public void testGetPublishedBots() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPublishedBots(AiBot.PublishingType.SKILL)).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPublishedBots(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPublishedBots_DBFail() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPublishedBots(AiBot.PublishingType.SKILL)).thenThrow(DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getPublishedBots(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBots_NoBots() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPublishedBots(AiBot.PublishingType.SKILL)).thenReturn(Collections.emptyList());
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPublishedBots(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getBotList().isEmpty());
    }

    @Test
    public void testGetPurchasedBots() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPurchasedBots(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPurchasedBots_DBException() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getPurchasedBots(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetPurchasedBots_NoBots() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPurchasedBots(DEVID_UUID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getBotList().isEmpty());
    }

    @Test
    public void testPurchaseBot() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseMarketplace.purchaseBot(any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(ALTERNATE_DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_DBException() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseMarketplace.purchaseBot(any(), anyInt())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(ALTERNATE_DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_botNotFound() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID_UUID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_botNotPublished() throws DatabaseException {
        final AiBot bot = new AiBot(SAMPLEBOT);
        bot.setPublished(AiBot.PublishingState.NOT_PUBLISHED);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseMarketplace.purchaseBot(any(), anyInt())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID_UUID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_tryPurchaseOwnBot() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseMarketplace.purchaseBot(any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(SAMPLEBOT.getDevId(), BOTID);
        // For now this should succeed to allow users to combine their own bots
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_insertReturnsNoRows() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseMarketplace.purchaseBot(any(), anyInt())).thenReturn(false);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(ALTERNATE_DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_AlreadyPurchased() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID_UUID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_invalidBot() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseMarketplace.purchaseBot(any(), anyInt())).thenReturn(false);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID_UUID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetBotDetails() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        ApiAiBot result = (ApiAiBot) this.aiBotStoreLogic.getBotDetails(DEVID_UUID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBot().getBotId());
    }

    @Test
    public void testGetBotDetails_botNotFound() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.getBotDetails(DEVID_UUID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_skill() throws DatabaseException {
        final int newBotId = 987654;
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabaseMarketplace.publishBot(any(), any())).thenReturn(newBotId);
        ApiAiBot result = (ApiAiBot) TestBotHelper.publishSampleBotSkill(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertNotNull(result.getBot());
        Assert.assertEquals(newBotId, result.getBot().getBotId());
        Assert.assertEquals(SAMPLEBOT.getDevId(), result.getBot().getDevId());
        Assert.assertEquals(AiBot.PublishingState.SUBMITTED, result.getBot().getPublishingState());
    }

    @Test
    public void testPublishBot_skill_errorInsert() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabaseMarketplace.publishBot(any(), any())).thenReturn(-1);
        ApiResult result = publishSampleBotSkill(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_skill_alreadyPublished() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenReturn(TestBotHelper.SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        ApiResult result = publishSampleBotSkill(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_skill_DBException() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabaseMarketplace.publishBot(any(), any())).thenThrow(DatabaseException.class);
        ApiResult result = publishSampleBotSkill(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_skill_aiNotTrained() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_UNDEFINED, false));
        ApiResult result = publishSampleBotSkill(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_skill_publishAsSkill_botsLinked() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(TestBotHelper.SAMPLEBOT));
        ApiResult result = publishSampleBotSkill(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_skill_noDevInfo() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(null);
        ApiResult result = publishSampleBotSkill(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_template() throws DatabaseException {
        final int newBotId = 876543;
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_UNDEFINED, false));
        when(this.fakeDatabaseMarketplace.publishBot(any(), any())).thenReturn(newBotId);
        when(this.fakeDatabaseMarketplace.addBotTemplate(anyInt(), any(), any(), any())).thenReturn(true);
        ApiAiBot result = (ApiAiBot) TestBotHelper.publishSampleBotTemplate(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertNotNull(result.getBot());
        Assert.assertEquals(newBotId, result.getBot().getBotId());
        Assert.assertEquals(SAMPLEBOT.getDevId(), result.getBot().getDevId());
        Assert.assertEquals(AiBot.PublishingState.SUBMITTED, result.getBot().getPublishingState());
        verify(this.fakeDatabaseTransaction).commit();
    }

    @Test
    public void testPublishBot_template_errorPublishing() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_UNDEFINED, false));
        when(this.fakeDatabaseMarketplace.publishBot(any(), any())).thenReturn(-1);
        ApiResult result = TestBotHelper.publishSampleBotTemplate(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeDatabaseTransaction).rollback();
    }

    @Test
    public void testPublishBot_template_errorWritingTemplate() throws DatabaseException {
        final int newBotId = 876543;
        when(this.fakeDatabaseMarketplace.getDeveloperInfo(any())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_UNDEFINED, false));
        when(this.fakeDatabaseMarketplace.publishBot(any(), any())).thenReturn(newBotId);
        when(this.fakeDatabaseMarketplace.addBotTemplate(anyInt(), any(), any(), any())).thenReturn(false);
        ApiResult result = TestBotHelper.publishSampleBotTemplate(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
        verify(this.fakeDatabaseTransaction).rollback();
    }

    @Test
    public void testGetBotDetails_DBException() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getBotDetails(DEVID_UUID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetBotTemplate() throws DatabaseException {
        final BotStructure botStructure = getBotStructure();
        final String template = getBotStructureTemplate();
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setPublishingType(AiBot.PublishingType.TEMPLATE);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseMarketplace.getBotTemplate(anyInt())).thenReturn(template);
        when(this.fakeJsonSerializer.deserialize(template, BotStructure.class)).thenReturn(botStructure);
        ApiBotStructure result = (ApiBotStructure) this.aiBotStoreLogic.getBotTemplate(DEVID_UUID, 1);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(botStructure.getName(), result.getBotStructure().getName());
        Assert.assertEquals(botStructure.getDescription(), result.getBotStructure().getDescription());
        Assert.assertEquals(botStructure.getTrainingFile(), result.getBotStructure().getTrainingFile());
    }

    @Test
    public void testGetBotTemplate_invalidBotId() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotTemplate(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.getBotTemplate(DEVID_UUID, 1);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetBotTemplate_errorInSerialization() throws DatabaseException {
        String template = getBotStructureTemplate();
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setPublishingType(AiBot.PublishingType.TEMPLATE);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseMarketplace.getBotTemplate(anyInt())).thenReturn(template);
        when(this.fakeJsonSerializer.deserialize(template, BotStructure.class)).thenThrow(JsonParseException.class);
        ApiResult result = this.aiBotStoreLogic.getBotTemplate(DEVID_UUID, 1);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetBotTemplate_notTemplate() throws DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setPublishingType(AiBot.PublishingType.SKILL);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        ApiResult result = this.aiBotStoreLogic.getBotTemplate(DEVID_UUID, 1);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testGetBotTemplate_noTemplateInDb() throws DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setPublishingType(AiBot.PublishingType.TEMPLATE);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseMarketplace.getBotTemplate(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.getBotTemplate(DEVID_UUID, 1);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetBotIcon() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotIconPath(anyInt())).thenReturn(BOT_ICON_PATH);
        ApiString result = (ApiString) this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(BOT_ICON_PATH, result.getString());
    }

    @Test
    public void testGetBotIcon_invalidBotId() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotIconPath(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetBotIcon_DBException() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotIconPath(anyInt())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }


    @Test
    public void testUploadBotIcon() throws DatabaseException {
        // this test will never pass in windows because of posix file system commands
        org.junit.Assume.assumeTrue(!SystemUtils.IS_OS_WINDOWS);

        prepareBotForUpload();
        when(this.fakeDatabaseMarketplace.saveBotIconPath(any(), anyInt(), any())).thenReturn(true);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID_UUID, BOTID, this.botIconStream, this.iconContentDisp);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_DBException() throws DatabaseException {

        // this test will never pass in windows because of posix file system commands
        org.junit.Assume.assumeTrue(!SystemUtils.IS_OS_WINDOWS);

        // We need to use a different botId from the one used in testUploadBotIcon to avoid write
        // contention due to using the same final filename.
        final int botId = BOTID + 1;
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(DEVID_UUID);
        bot.setBotId(botId);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseMarketplace.saveBotIconPath(any(), anyInt(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID_UUID, botId, this.botIconStream, this.iconContentDisp);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_invalid_botId() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.saveBotIconPath(any(), anyInt(), any())).thenReturn(false);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID_UUID, BOTID, this.botIconStream, this.iconContentDisp);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_iconSize_overLimit() throws DatabaseException {
        prepareBotForUpload();
        ByteArrayInputStream bigStream = new ByteArrayInputStream(
                new byte[(int) (AIBotStoreLogic.MAX_ICON_FILE_SIZE + 1)]);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID_UUID, BOTID, bigStream, this.iconContentDisp);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    private void prepareBotForUpload() throws DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(DEVID_UUID);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
    }

    private static BotStructure getBotStructure() {
        return new BotStructure("bot", "desc", Collections.emptyList(), "aaa\nbbb", new HashMap<>(),
                1, false, 1, 1.0, 1, Locale.UK.toLanguageTag(), "UTC", Collections.singletonList("Dunno"), "",
                Collections.emptyList(), "clientToken", -1, -1, "handoverMessage");
    }

    public static String getBotStructureTemplate() {
        JsonSerializer serializer = new JsonSerializer();
        return serializer.serialize(getBotStructure());
    }
}
