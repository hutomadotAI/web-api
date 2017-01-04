package com.hutoma.api.logic;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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
}