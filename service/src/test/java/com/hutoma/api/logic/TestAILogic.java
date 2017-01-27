package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiAiBotList;
import com.hutoma.api.containers.ApiAiList;
import com.hutoma.api.containers.ApiResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import javax.ws.rs.core.SecurityContext;

import static com.hutoma.api.common.BotHelper.BOTID;
import static com.hutoma.api.common.BotHelper.SAMPLEBOT;
import static com.hutoma.api.common.TestDataHelper.AIID;
import static com.hutoma.api.common.TestDataHelper.DEVID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

/**
 * Unit tests for the AI logic.
 */
public class TestAILogic {

    private static final String VALIDKEY = "RW1wdHlUZXN0S2V5";
    private static final String VALIDDEVID = "DevidExists";

    SecurityContext fakeContext;
    Database fakeDatabase;
    AIServices fakeAiServices;
    Config fakeConfig;
    Tools fakeTools;
    AILogic aiLogic;
    ILogger fakeLogger;
    JsonSerializer fakeSerializer;

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
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
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyObject(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), anyObject())).thenReturn(TestDataHelper.AIID);
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
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyObject(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), anyObject())).thenThrow(new Database.DatabaseException(new Exception("test")));
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
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiAi result = (ApiAi) this.aiLogic.getSingleAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(AIID.toString(), result.getAiid());
    }

    @Test
    public void testGetSingle_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenThrow(new Database.DatabaseException(new Exception("")));
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_DB_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.getSingleAI(this.fakeContext, VALIDDEVID, AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(eq(VALIDDEVID), any())).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid_Return() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(eq(VALIDDEVID), any())).thenReturn(returnList);
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
        when(this.fakeDatabase.getAllAIs(eq(this.VALIDDEVID), any())).thenReturn(new ArrayList<ApiAi>());
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_DBFail() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(anyString(), any())).thenThrow(new Database.DatabaseException(new Exception("test")));
        this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        ApiResult result = this.aiLogic.getAIs(this.fakeContext, VALIDDEVID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDelete_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(anyString(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
    }

    @Test
    public void testDelete_DBFail_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(anyString(), any())).thenReturn(false);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
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
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
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
    public void testLinkBotToAi() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenReturn(true);
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTraining(DEVID, AIID);
    }

    @Test
    public void testLinkBotToAi_trainingInProgress() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenReturn(true);
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices).stopTrainingIfNeeded(DEVID, AIID);
    }

    @Test
    public void testLinkBotToAi_DB_failed_update() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenReturn(false);
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenThrow(Database.DatabaseException.class);
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_aiTrainingInProgress_AIException()
            throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabase.linkBotToAi(anyString(), any(), anyInt())).thenReturn(true);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeAiServices).stopTraining(anyString(), any());
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botNotPurchased() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.emptyList());
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botAlreadyLinked() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(anyString())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(anyString(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botNotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(null);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTraining(DEVID, AIID);
    }

    @Test
    public void testUnlinkBotFromAi_aiTrainingInProgress()
            throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices).stopTrainingIfNeeded(DEVID, AIID);
    }

    @Test
    public void testUnlinkBotFromAi_DB_failed_update() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenReturn(false);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_aiTrainingFile_AIException()
            throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAI(anyString(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabase.unlinkBotFromAi(anyString(), any(), anyInt())).thenReturn(true);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeAiServices).stopTraining(anyString(), any());
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
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
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), anyString(), anyBoolean(), anyObject(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), anyObject())).thenReturn(aiid);
    }

    private ArrayList<ApiAi> getAIList() {
        ArrayList<ApiAi> returnList = new ArrayList<>();
        returnList.add(TestDataHelper.getSampleAI());
        return returnList;
    }
}

