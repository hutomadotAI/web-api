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
    public void testGetAll_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiBotStoreLogic.getPublishedBots();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetAll_DBFail() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiBotStoreLogic.getPublishedBots();
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_NoBots() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBots()).thenReturn(Collections.emptyList());
        ApiResult result = this.aiBotStoreLogic.getPublishedBots();
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }
}