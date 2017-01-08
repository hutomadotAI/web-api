package com.hutoma.api.logic;

import com.hutoma.api.common.BotHelper;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiStreamResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;

import static com.hutoma.api.common.BotHelper.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AI Bot Store logic.
 */
public class TestAIBotstoreLogic {

    private final ByteArrayInputStream botIconStream = new ByteArrayInputStream(BotHelper.getBotIconContent());
    private Database fakeDatabase;
    private AIBotStoreLogic aiBotStoreLogic;
    private Tools fakeTools;

    @Before
    public void setup() {
        this.fakeDatabase = mock(Database.class);
        this.fakeTools = mock(Tools.class);
        this.aiBotStoreLogic = new AIBotStoreLogic(this.fakeDatabase, mock(ILogger.class), this.fakeTools);
    }

    @Test
    public void testGetPublishedBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPublishedBots();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetPublishedBots_DBFail() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getPublishedBots();
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBots_NoBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenReturn(Collections.emptyList());
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPublishedBots();
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
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenReturn(true);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.purchaseBot(anyString(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.purchaseBot(DEVID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testPurchaseBot_AlreadyPurchased() throws Database.DatabaseException {
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
        ApiAiBot result = (ApiAiBot) this.aiBotStoreLogic.getBotDetails(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBot().getBotId());
    }

    @Test
    public void testGetBotDetails_botNotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.getBotDetails(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot() throws Database.DatabaseException {
        final int newBotId = 987654;
        when(this.fakeDatabase.publishBot(any())).thenReturn(newBotId);
        ApiAiBot result = (ApiAiBot) BotHelper.publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertNotNull(result.getBot());
        Assert.assertEquals(newBotId, result.getBot().getBotId());
        Assert.assertEquals(SAMPLEBOT.getDevId(), result.getBot().getDevId());
    }

    @Test
    public void testPublishBot_errorInsert() throws Database.DatabaseException {
        when(this.fakeDatabase.publishBot(any())).thenReturn(-1);
        ApiResult result = publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.publishBot(any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = publishSampleBot(this.aiBotStoreLogic);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetBotDetails_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getBotDetails(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetBotIcon() throws Database.DatabaseException, IOException {
        when(this.fakeDatabase.getBotIcon(anyInt())).thenReturn(this.botIconStream);
        ApiStreamResult result = (ApiStreamResult) this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertNotNull(result.getStream());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        result.getStream().write(outStream);
        Assert.assertEquals(BotHelper.getBotIconContentSize(), outStream.size());
        byte[] icon = outStream.toByteArray();
        byte[] expected = BotHelper.getBotIconContent();
        for (int i = 0; i < BotHelper.getBotIconContentSize(); i++) {
            Assert.assertEquals(expected[i], icon[i]);
        }
    }

    @Test
    public void testGetBotIcon_invalidBotId() throws Database.DatabaseException, IOException {
        when(this.fakeDatabase.getBotIcon(anyInt())).thenReturn(null);
        ApiResult result = this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetBotIcon_DBException() throws Database.DatabaseException, IOException {
        when(this.fakeDatabase.getBotIcon(anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getBotIcon(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon() throws Database.DatabaseException, IOException {
        when(this.fakeTools.isStreamSmallerThan(any(), anyLong())).thenReturn(true);
        when(this.fakeDatabase.saveBotIcon(anyString(), anyInt(), any())).thenReturn(true);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID, BOTID, this.botIconStream);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_DBException() throws Database.DatabaseException, IOException {
        when(this.fakeTools.isStreamSmallerThan(any(), anyLong())).thenReturn(true);
        when(this.fakeDatabase.saveBotIcon(anyString(), anyInt(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID, BOTID, this.botIconStream);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_invalid_botId() throws Database.DatabaseException, IOException {
        when(this.fakeTools.isStreamSmallerThan(any(), anyLong())).thenReturn(true);
        when(this.fakeDatabase.saveBotIcon(anyString(), anyInt(), any())).thenReturn(false);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID, BOTID, this.botIconStream);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUploadBotIcon_iconSize_overLimit() throws Database.DatabaseException, IOException {
        when(this.fakeTools.isStreamSmallerThan(any(), anyLong())).thenReturn(false);
        ApiResult result = this.aiBotStoreLogic.uploadBotIcon(DEVID, BOTID, this.botIconStream);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }
}