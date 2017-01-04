package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAiBot;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AI Bot Store logic.
 */
public class TestAIBotstoreLogic {

    private static final String DEVID = "devid";
    private static final UUID AIID = UUID.randomUUID();
    private static final int BOTID = 1234;

    private static final AiBot SAMPLEBOT =
            new AiBot(DEVID, AIID, BOTID, "name", "description", "long description", "alert message", "badge",
                    BigDecimal.valueOf(1.123), "sample", "category", DateTime.now(), "privacy policy",
                    "classification", "version", "http://video", true);

    private Database fakeDatabase;
    private AIBotStoreLogic aiBotStoreLogic;

    @Before
    public void setup() {
        this.fakeDatabase = mock(Database.class);
        this.aiBotStoreLogic = new AIBotStoreLogic(this.fakeDatabase, mock(ILogger.class));
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
        ApiAiBot result = (ApiAiBot) publishSampleBot();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertNotNull(result.getBot());
        Assert.assertEquals(newBotId, result.getBot().getBotId());
        Assert.assertEquals(SAMPLEBOT.getDevId(), result.getBot().getDevId());
    }

    @Test
    public void testPublishBot_errorInsert() throws Database.DatabaseException {
        when(this.fakeDatabase.publishBot(any())).thenReturn(-1);
        ApiResult result = publishSampleBot();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testPublishBot_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.publishBot(any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = publishSampleBot();
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetBotDetails_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getBotDetails(BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private ApiResult publishSampleBot() {
        return this.aiBotStoreLogic.publishBot(SAMPLEBOT.getDevId(), SAMPLEBOT.getAiid(), SAMPLEBOT.getName(),
                SAMPLEBOT.getDescription(), SAMPLEBOT.getLongDescription(), SAMPLEBOT.getAlertMessage(), SAMPLEBOT.getBadge(),
                SAMPLEBOT.getPrice(), SAMPLEBOT.getSample(), SAMPLEBOT.getCategory(), SAMPLEBOT.getPrivacyPolicy(),
                SAMPLEBOT.getClassification(), SAMPLEBOT.getVersion(), SAMPLEBOT.getVideoLink());
    }
}