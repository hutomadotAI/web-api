package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.DeveloperInfoHelper;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestBotHelper;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiString;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;

import static com.hutoma.api.common.TestBotHelper.*;
import static com.hutoma.api.common.TestDataHelper.DEVID;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AI Bot Store logic.
 */
public class TestAIBotstoreLogic {

    private static final String ALTERNATE_DEVID = "other_devId";
    private static final String BOT_ICON_PATH = BOTID + ".png";
    private final ByteArrayInputStream botIconStream = new ByteArrayInputStream(TestBotHelper.getBotIconContent());
    private Database fakeDatabase;
    private AIBotStoreLogic aiBotStoreLogic;
    private Config fakeConfig;
    private FormDataContentDisposition iconContentDisp;

    @Before
    public void setup() {
        this.fakeDatabase = mock(Database.class);
        this.fakeConfig = mock(Config.class);
        this.aiBotStoreLogic = new AIBotStoreLogic(this.fakeDatabase, mock(ILogger.class), this.fakeConfig,
                mock(JsonSerializer.class));
        this.iconContentDisp = FormDataContentDisposition.name("file").fileName(BOT_ICON_PATH).build();
        // Store any bot icons in the temp folder
        when(this.fakeConfig.getBotIconStoragePath()).thenReturn(System.getProperty("java.io.tmpdir"));
    }

    @Test
    public void testGetPublishedBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPublishedBots(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPublishedBots_DBFail() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getPublishedBots(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBots_NoBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenReturn(Collections.emptyList());
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPublishedBots(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getBotList().isEmpty());
    }

    @Test
    public void testGetPurchasedBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPurchasedBots(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPurchasedBots_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getPurchasedBots(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetPurchasedBots_NoBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPurchasedBots(DEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getBotList().isEmpty());
    }

    @Test
    public void testPurchaseBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenReturn(true);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(ALTERNATE_DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(ALTERNATE_DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_botNotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_botNotPublished() throws Database.DatabaseException {
        final AiBot bot = new AiBot(SAMPLEBOT);
        bot.setPublished(AiBot.PublishingState.NOT_PUBLISHED);
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_tryPurchaseOwnBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenReturn(true);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(SAMPLEBOT.getDevId(), BOTID);
        // For now this should succeed to allow users to combine their own bots
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_insertReturnsNoRows() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenReturn(false);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(ALTERNATE_DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_AlreadyPurchased() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_invalidBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenReturn(false);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetBotDetails() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        ApiAiBot result = (ApiAiBot) this.aiBotStoreLogic.getBotDetails(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBot().getBotId());
    }

    @Test
    public void testGetBotDetails_botNotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.getBotDetails(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot() throws Database.DatabaseException {
        final int newBotId = 987654;
        when(this.fakeDatabase.getDeveloperInfo(anyString())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabase.publishBot(any())).thenReturn(newBotId);
        ApiAiBot result = (ApiAiBot) TestBotHelper.publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertNotNull(result.getBot());
        Assert.assertEquals(newBotId, result.getBot().getBotId());
        Assert.assertEquals(SAMPLEBOT.getDevId(), result.getBot().getDevId());
        Assert.assertEquals(AiBot.PublishingState.SUBMITTED, result.getBot().getPublishingState());
    }

    @Test
    public void testPublishBot_errorInsert() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(anyString())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabase.publishBot(any())).thenReturn(-1);
        ApiResult result = publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_alreadyPublished() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBotForAI(anyString(), any())).thenReturn(TestBotHelper.SAMPLEBOT);
        when(this.fakeDatabase.getDeveloperInfo(anyString())).thenReturn(DeveloperInfoHelper.DEVINFO);
        ApiResult result = publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(anyString())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_TRAINING_COMPLETE, false));
        when(this.fakeDatabase.publishBot(any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_aiNotTrained() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(anyString())).thenReturn(DeveloperInfoHelper.DEVINFO);
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(
                TestDataHelper.getAi(TrainingStatus.AI_UNDEFINED, false));
        ApiResult result = publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_botsLinked() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.singletonList(TestBotHelper.SAMPLEBOT));
        ApiResult result = publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_noDevInfo() throws Database.DatabaseException {
        when(this.fakeDatabase.getDeveloperInfo(anyString())).thenReturn(null);
        ApiResult result = publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testGetBotDetails_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getBotDetails(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetBotIcon() throws Database.DatabaseException, IOException {
        when(this.fakeDatabase.getBotIconPath(anyInt())).thenReturn(BOT_ICON_PATH);
        ApiString result = (ApiString) this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(BOT_ICON_PATH, result.getString());
    }

    @Test
    public void testGetBotIcon_invalidBotId() throws Database.DatabaseException, IOException {
        when(this.fakeDatabase.getBotIconPath(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetBotIcon_DBException() throws Database.DatabaseException, IOException {
        when(this.fakeDatabase.getBotIconPath(anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon() throws Database.DatabaseException, IOException {
        prepareBotForUpload();
        when(this.fakeDatabase.saveBotIconPath(anyString(), anyInt(), any())).thenReturn(true);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID, BOTID, this.botIconStream, this.iconContentDisp);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_DBException() throws Database.DatabaseException, IOException {
        // We need to use a different botId from the one used in testUploadBotIcon to avoid write
        // contention due to using the same final filename.
        final int botId = BOTID + 1;
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(DEVID);
        bot.setBotId(botId);
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabase.saveBotIconPath(anyString(), anyInt(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID, botId, this.botIconStream, this.iconContentDisp);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_invalid_botId() throws Database.DatabaseException, IOException {
        when(this.fakeDatabase.saveBotIconPath(anyString(), anyInt(), any())).thenReturn(false);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID, BOTID, this.botIconStream, this.iconContentDisp);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_iconSize_overLimit() throws Database.DatabaseException, IOException {
        prepareBotForUpload();
        ByteArrayInputStream bigStream = new ByteArrayInputStream(
                new byte[(int) (AIBotStoreLogic.MAX_ICON_FILE_SIZE + 1)]);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID, BOTID, bigStream, this.iconContentDisp);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    private void prepareBotForUpload() throws Database.DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(DEVID);
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(bot);
    }
}