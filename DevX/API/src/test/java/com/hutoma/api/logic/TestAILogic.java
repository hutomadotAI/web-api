package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.TrainingStatus;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import javax.ws.rs.core.SecurityContext;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AI logic.
 */
public class TestAILogic {

    private static final String DEVID = "devid";
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final int BOTID = 1234;
    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final String VALIDDEVID = "DevidExists";
    private static final String AI_ENGINE = "MOCKENGINE";
    private static final AiBot SAMPLEBOT =
            new AiBot(DEVID, AIID, BOTID, "name", "description", "long description", "alert message", "badge",
                    BigDecimal.valueOf(1.123), "sample", "category", DateTime.now(), "privacy policy",
                    "classification", "version", "http://video", true);
    //http://mockito.org/
    FakeJsonSerializer fakeSerializer;
    SecurityContext fakeContext;
    Database fakeDatabase;
    AIServices fakeAiServices;
    Config fakeConfig;
    Tools fakeTools;
    AILogic aiLogic;
    ILogger fakeLogger;

    @Before
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeAiServices = mock(AIServices.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(ILogger.class);

        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        this.aiLogic = new AILogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase, this.fakeAiServices,
                this.fakeLogger, this.fakeTools);
    }

    @Test
    public void testCreate_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), any(), anyString(),
                anyObject(), anyObject(), anyDouble(), anyInt(), anyInt())).thenReturn(AIID);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        ApiResult result = this.aiLogic.createAI(this.fakeContext, DEVID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testCreate_Valid_Token() throws Database.DatabaseException {
        whenCreateAiReturn(AIID);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        ApiResult result = this.aiLogic.createAI(this.fakeContext, DEVID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertTrue(result instanceof ApiAi);
        Assert.assertNotNull(((ApiAi) result).getClient_token());
        Assert.assertFalse(((ApiAi) result).getClient_token().isEmpty());
    }

    @Test
    public void testCreate_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), any(), anyString(),
                anyObject(), anyObject(), anyDouble(), anyInt(), anyInt())).thenThrow(new Database.DatabaseException(new Exception("test")));
        ApiResult result = this.aiLogic.createAI(this.fakeContext, DEVID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testCreate_DB_NameClash() throws Database.DatabaseException {
        whenCreateAiReturn(UUID.randomUUID());
        ApiResult result = this.aiLogic.createAI(this.fakeContext, DEVID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(400, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any())).thenReturn(getAI());
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any())).thenReturn(getAI());
        ApiAi result = (ApiAi) this.aiLogic.getSingleAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(this.AIID.toString(), result.getAiid());
    }

    @Test
    public void testGetSingle_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any())).thenThrow(new Database.DatabaseException(new Exception("")));
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_DB_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(VALIDDEVID)).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid_Return() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(VALIDDEVID)).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        Assert.assertTrue(result instanceof ApiAiList);
        ApiAiList list = (ApiAiList) result;
        Assert.assertNotNull(list.getAiList());
        Assert.assertFalse(list.getAiList().isEmpty());
        Assert.assertEquals(AIID.toString(), list.getAiList().get(0).getAiid());
    }

    @Test
    public void testGetAll_NoneFound() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(VALIDDEVID)).thenReturn(new ArrayList<ApiAi>());
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_DBFail() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(anyString())).thenThrow(new Database.DatabaseException(new Exception("test")));
        this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDelete_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(anyString(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.deleteAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testDelete_DBFail_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(anyString(), any())).thenReturn(false);
        ApiResult result = this.aiLogic.deleteAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAI(anyString(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.updateAI(this.fakeContext, DEVID, AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_dbFail() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAI(anyString(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt())).thenReturn(false);
        ApiResult result = this.aiLogic.updateAI(this.fakeContext, DEVID, AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_dbException() throws Database.DatabaseException {
        when(this.fakeDatabase.updateAI(anyString(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.updateAI(this.fakeContext, DEVID, AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString());
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDelete_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(anyString(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.deleteAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus() throws Database.DatabaseException {
        AiStatus status = new AiStatus(DEVID, AIID, TrainingStatus.NOT_STARTED, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyString(), any(), any(), anyString(), anyDouble(), anyDouble())).thenReturn(true);
        ApiResult result = this.aiLogic.updateAIStatus(this.fakeContext, status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_db_returns_false() throws Database.DatabaseException {
        AiStatus status = new AiStatus(DEVID, AIID, TrainingStatus.NOT_STARTED, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyString(), any(), any(), anyString(), anyDouble(), anyDouble())).thenReturn(false);
        ApiResult result = this.aiLogic.updateAIStatus(this.fakeContext, status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_dbException() throws Database.DatabaseException {
        AiStatus status = new AiStatus(DEVID, AIID, TrainingStatus.NOT_STARTED, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyString(), any(), any(), anyString(), anyDouble(), anyDouble())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.updateAIStatus(this.fakeContext, status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBots_valid() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiLogic.getLinkedBots(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetLinkedBots_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.getLinkedBots(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBots_noResults() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.emptyList());
        ApiAiBotList result = (ApiAiBotList) this.aiLogic.getLinkedBots(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getBotList().isEmpty());
    }

    @Test
    public void testLinkBotToAi() throws Database.DatabaseException {
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_DB_failed_update() throws Database.DatabaseException {
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenReturn(false);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi() throws Database.DatabaseException {
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_DB_failed_update() throws Database.DatabaseException {
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenReturn(false);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBotForAI_hasBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBotForAI(anyString(), any())).thenReturn(SAMPLEBOT);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBotForAI_hasNoPublishedBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBotForAI(anyString(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBotForAI_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBotForAI(anyString(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private void whenCreateAiReturn(UUID aiid) throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyDouble(), anyInt(), anyInt(), any(), anyString(),
                anyObject(), anyObject(), anyDouble(), anyInt(), anyInt())).thenReturn(aiid);
    }

    private ApiAi getAI() {
        return new ApiAi(AIID.toString(), "token", "name", "desc", DateTime.now(), false, 0.5, "debuginfo",
                "trainstatus", null, "", 0, 0.0, 1, Locale.getDefault(),
                "UTC");
    }

    private ArrayList<ApiAi> getAIList() {
        ArrayList<ApiAi> returnList = new ArrayList<>();
        returnList.add(getAI());
        return returnList;
    }
}

