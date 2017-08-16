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
import com.hutoma.api.containers.sub.AiBot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import javax.inject.Provider;

import static com.hutoma.api.common.TestBotHelper.BOTID;
import static com.hutoma.api.common.TestBotHelper.SAMPLEBOT;
import static com.hutoma.api.common.TestDataHelper.*;
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
    private static final UUID VALIDDEVID = UUID.fromString("0a5c30c3-cd10-45da-9be9-e57942660215");
    Provider<AIIntegrationLogic> fakeAiIntegrationLogicProvider;
    private Database fakeDatabase;
    private AIServices fakeAiServices;
    private Config fakeConfig;
    private Tools fakeTools;
    private AILogic aiLogic;
    private ILogger fakeLogger;
    private JsonSerializer fakeSerializer;

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getEncodingKey()).thenReturn(VALIDKEY);
        this.fakeDatabase = mock(Database.class);
        this.fakeAiServices = mock(AIServices.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeAiIntegrationLogicProvider = mock(Provider.class);
        when(this.fakeAiIntegrationLogicProvider.get()).thenReturn(mock(AIIntegrationLogic.class));

        when(this.fakeConfig.getMaxLinkedBotsPerAi()).thenReturn(5);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        this.aiLogic = new AILogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase, this.fakeAiServices,
                this.fakeLogger, this.fakeTools, this.fakeAiIntegrationLogicProvider);
    }

    @Test
    public void testCreate_Valid() throws Database.DatabaseException {
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), any(), anyBoolean(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), anyObject())).thenReturn(TestDataHelper.AIID);
        ApiResult result = this.aiLogic.createAI(DEVID_UUID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testCreate_Valid_Token() throws Database.DatabaseException {
        whenCreateAiReturn(AIID);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        ApiResult result = this.aiLogic.createAI(DEVID_UUID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertTrue(result instanceof ApiAi);
        Assert.assertNotNull(((ApiAi) result).getClient_token());
        Assert.assertFalse(((ApiAi) result).getClient_token().isEmpty());
    }

    @Test
    public void testCreate_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), any(), anyBoolean(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), anyObject())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.createAI(DEVID_UUID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testCreate_DB_NameClash() throws Database.DatabaseException {
        whenCreateAiReturn(UUID.randomUUID());
        ApiResult result = this.aiLogic.createAI(DEVID_UUID, "name", "description", true, 0, 0.0, 1, null, "");
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiResult result = this.aiLogic.getSingleAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid_Return() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiAi result = (ApiAi) this.aiLogic.getSingleAI(VALIDDEVID, AIID);
        Assert.assertEquals(AIID.toString(), result.getAiid());
    }

    @Test
    public void testGetSingle_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.getSingleAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_DB_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.getSingleAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(eq(VALIDDEVID), any())).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(VALIDDEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid_Return() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(eq(VALIDDEVID), any())).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(VALIDDEVID);
        Assert.assertTrue(result instanceof ApiAiList);
        ApiAiList list = (ApiAiList) result;
        Assert.assertNotNull(list.getAiList());
        Assert.assertFalse(list.getAiList().isEmpty());
        Assert.assertEquals(AIID.toString(), list.getAiList().get(0).getAiid());
    }

    @Test
    public void testGetAll_NoneFound() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(eq(VALIDDEVID), any())).thenReturn(new ArrayList<>());
        ApiResult result = this.aiLogic.getAIs(VALIDDEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_DBFail() throws Database.DatabaseException {
        ArrayList<ApiAi> returnList = getAIList();
        when(this.fakeDatabase.getAllAIs(any(), any())).thenThrow(Database.DatabaseException.class);
        this.aiLogic.getAIs(VALIDDEVID);
        ApiResult result = this.aiLogic.getAIs(VALIDDEVID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testDelete_Valid() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getPublishedBotForAI(any(), any())).thenReturn(null);
        when(this.fakeDatabase.deleteAi(any(), any())).thenReturn(true);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(200, result.getStatus().getCode());
        // Make sure we request it to be deleted by the backends
        verify(this.fakeAiServices).deleteAI(any(), any(), any());
    }

    @Test
    public void testDelete_DBFail_NotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.deleteAi(any(), any())).thenReturn(false);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(404, result.getStatus().getCode());
    }

    @Test
    public void testDelete_servicesException_notFound() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.deleteAi(any(), any())).thenReturn(true);
        ServerConnector.AiServicesException exMain = new ServerConnector.AiServicesException("main");
        ServerConnector.AiServicesException exSub = new ServerConnector.AiServicesException("sub", HttpURLConnection.HTTP_NOT_FOUND);
        exMain.addSuppressed(exSub);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        doThrow(exMain).when(this.fakeAiServices).deleteAI(any(), any(), any());
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testDelete_servicesException_other() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.deleteAi(any(), any())).thenReturn(true);
        ServerConnector.AiServicesException exMain = new ServerConnector.AiServicesException("main");
        ServerConnector.AiServicesException exSub = new ServerConnector.AiServicesException("sub", HttpURLConnection.HTTP_INTERNAL_ERROR);
        exMain.addSuppressed(exSub);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        doThrow(exMain).when(this.fakeAiServices).deleteAI(any(), any(), any());
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDelete_withPublishedBot_botNotPurchased_canBeDeleted() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.deleteAi(any(), any())).thenReturn(true);
        when(this.fakeDatabase.hasBotBeenPurchased(anyInt())).thenReturn(false);
        when(this.fakeDatabase.getPublishedBotForAI(any(), any())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeDatabase).deleteAi(any(), any());
        verify(this.fakeAiServices).deleteAI(any(), any(), any());
    }

    @Test
    public void testDelete_withPublishedBot_botPurchased_cannotBeDeleted() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getPublishedBotForAI(any(), any())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.hasBotBeenPurchased(anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        // Verify we don't actually request it to be deleted by the backends
        verify(this.fakeAiServices, never()).deleteAI(any(), any(), any());
        verify(this.fakeDatabase, never()).deleteAi(any(), any());
    }

    @Test
    public void testUpdateAi() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        when(this.fakeDatabase.updateAI(any(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt(), any(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.updateAI(DEVID_UUID, AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString(), DEFAULT_CHAT_RESPONSES);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_dbFail() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        when(this.fakeDatabase.updateAI(any(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt(), any(), any())).thenReturn(false);
        ApiResult result = this.aiLogic.updateAI(DEVID_UUID, AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString(), DEFAULT_CHAT_RESPONSES);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_dbException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        when(this.fakeDatabase.updateAI(any(), any(), anyString(), anyBoolean(),
                any(), anyString(), anyDouble(), anyInt(), anyInt(), any(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.updateAI(DEVID_UUID, AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString(), DEFAULT_CHAT_RESPONSES);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDelete_DBFail_Error() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabase.deleteAi(any(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(500, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBots_valid() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiLogic.getLinkedBots(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetLinkedBots_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.getLinkedBots(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBots_noResults() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        ApiAiBotList result = (ApiAiBotList) this.aiLogic.getLinkedBots(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getBotList().isEmpty());
    }

    @Test
    public void testLinkBotToAi() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(any(), any(), anyInt())).thenReturn(true);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTraining(null, DEVID_UUID, AIID);
    }

    @Test
    public void testLinkBotToAi_trainingInProgress() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(any(), any(), anyInt())).thenReturn(true);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices).stopTrainingIfNeeded(DEVID_UUID, AIID);
    }

    @Test
    public void testLinkBotToAi_DB_failed_update() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(any(), any(), anyInt())).thenReturn(false);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(any(), any(), anyInt())).thenThrow(Database.DatabaseException.class);
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_aiTrainingInProgress_AIException()
            throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabase.linkBotToAi(any(), any(), anyInt())).thenReturn(true);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeAiServices).stopTraining(any(), any(), any());
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botNotPurchased_notOwned() throws Database.DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(UUID.randomUUID());
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabase.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botNotPurchased_Owned() throws Database.DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(DEVID_UUID);
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabase.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabase.linkBotToAi(any(), any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botAlreadyLinked() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botNotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(null);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_maximumBotsReached() throws Database.DatabaseException {
        when(this.fakeDatabase.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabase.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        // Limit the maximum number of bots to 1, so that we're already at the limit
        when(this.fakeConfig.getMaxLinkedBotsPerAi()).thenReturn(1);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi() throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabase.unlinkBotFromAi(any(), any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTraining(null, DEVID_UUID, AIID);
    }

    @Test
    public void testUnlinkBotFromAi_aiTrainingInProgress()
            throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabase.unlinkBotFromAi(any(), any(), anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices).stopTrainingIfNeeded(DEVID_UUID, AIID);
    }

    @Test
    public void testUnlinkBotFromAi_DB_failed_update() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabase.unlinkBotFromAi(any(), any(), anyInt())).thenReturn(false);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabase.unlinkBotFromAi(any(), any(), anyInt())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_aiTrainingFile_AIException()
            throws Database.DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabase.unlinkBotFromAi(any(), any(), anyInt())).thenReturn(true);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeAiServices).stopTraining(any(), any(), any());
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }


    @Test
    public void testGetPublishedBotForAI_hasBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBotForAI(any(), any())).thenReturn(SAMPLEBOT);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBotForAI_hasNoPublishedBot() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBotForAI(any(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBotForAI_DBException() throws Database.DatabaseException {
        when(this.fakeDatabase.getPublishedBotForAI(any(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private void whenCreateAiReturn(UUID aiid) throws Database.DatabaseException {
        when(this.fakeDatabase.createAI(any(), anyString(), anyString(), any(), anyBoolean(),
                anyString(), anyObject(), anyObject(), anyDouble(), anyInt(),
                anyInt(), anyObject())).thenReturn(aiid);
    }

    private ArrayList<ApiAi> getAIList() {
        ArrayList<ApiAi> returnList = new ArrayList<>();
        returnList.add(TestDataHelper.getSampleAI());
        return returnList;
    }
}

