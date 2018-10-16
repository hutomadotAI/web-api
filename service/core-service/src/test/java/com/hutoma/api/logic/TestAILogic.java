package com.hutoma.api.logic;

import com.google.common.collect.ImmutableMap;
import com.hutoma.api.common.*;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.*;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.validation.ParameterValidationException;
import com.hutoma.api.validation.Validate;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import javax.inject.Provider;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.*;

import static com.hutoma.api.common.TestBotHelper.BOTID;
import static com.hutoma.api.common.TestBotHelper.SAMPLEBOT;
import static com.hutoma.api.common.TestDataHelper.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AI logic.
 */
public class TestAILogic {

    private static final UUID VALIDDEVID = UUID.fromString("0a5c30c3-cd10-45da-9be9-e57942660215");
    private Provider<AIIntegrationLogic> fakeAiIntegrationLogicProvider;
    private Provider<DatabaseTransaction> fakeDatabaseTransactionProvider;
    private DatabaseAI fakeDatabaseAi;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private DatabaseMarketplace fakeDatabaseMarketplace;
    private AIServices fakeAiServices;
    private Config fakeConfig;
    private Tools fakeTools;
    private AILogic aiLogic;
    private ILogger fakeLogger;
    private JsonSerializer fakeSerializer;
    private Validate fakeValidate;
    private DatabaseTransaction fakeTransaction;
    private FeatureToggler fakeFeatureToggler;
    private AIIntegrationLogic fakeIntegration;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getEncodingKey()).thenReturn(TestDataHelper.VALID_ENCODING_KEY);
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeDatabaseMarketplace = mock(DatabaseMarketplace.class);
        this.fakeAiServices = mock(AIServices.class);
        this.fakeTools = mock(Tools.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeValidate = mock(Validate.class);
        this.fakeAiIntegrationLogicProvider = mock(Provider.class);
        this.fakeDatabaseTransactionProvider = mock(Provider.class);
        this.fakeTransaction = mock(DatabaseTransaction.class);
        this.fakeFeatureToggler = mock(FeatureToggler.class);
        this.fakeIntegration = mock(AIIntegrationLogic.class);

        TestDataHelper.setFeatureToggleToControl(this.fakeFeatureToggler);

        when(this.fakeDatabaseTransactionProvider.get()).thenReturn(this.fakeTransaction);
        when(this.fakeAiIntegrationLogicProvider.get()).thenReturn(this.fakeIntegration);
        when(this.fakeConfig.getMaxLinkedBotsPerAi()).thenReturn(5);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        this.aiLogic = new AILogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabaseAi,
                this.fakeDatabaseEntitiesIntents, this.fakeDatabaseMarketplace, this.fakeAiServices, this.fakeLogger,
                this.fakeTools, this.fakeValidate, this.fakeAiIntegrationLogicProvider, this.fakeDatabaseTransactionProvider,
                this.fakeFeatureToggler);
    }

    @Test
    public void testCreate_Valid() throws DatabaseException {
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        TestDataHelper.mockDatabaseCreateAIInTrans(this.fakeDatabaseAi, TestDataHelper.AIID);
        ApiResult result = callDefaultCreateAI();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testCreate_Valid_Token() throws DatabaseException {
        TestDataHelper.mockDatabaseCreateAIInTrans(this.fakeDatabaseAi, AIID);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        ApiResult result = callDefaultCreateAI();
        Assert.assertTrue(result instanceof ApiAi);
        Assert.assertNotNull(((ApiAi) result).getClient_token());
        Assert.assertFalse(((ApiAi) result).getClient_token().isEmpty());
    }

    @Test
    public void testCreate_DBFail_Error() throws DatabaseException {
        when(this.fakeDatabaseAi.createAI(any(), anyString(), anyString(), any(), anyBoolean(),
                anyString(), any(), any(), anyDouble(), anyInt(),
                anyInt(), any(), anyInt(), anyInt(), anyString(), any())).thenThrow(DatabaseException.class);
        ApiResult result = callDefaultCreateAI();
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testCreate_DB_NameClash() throws DatabaseException {
        TestDataHelper.mockDatabaseCreateAIInTrans(this.fakeDatabaseAi, UUID.randomUUID());
        ApiResult result = callDefaultCreateAI();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testCreate_handoverMessageRequiredWhenThreshold() throws DatabaseException {
        TestDataHelper.mockDatabaseCreateAIInTrans(this.fakeDatabaseAi, UUID.randomUUID());
        ApiResult result = this.aiLogic.createAI(VALIDDEVID, "ainame", "desc", false, 1, 0.3, 1,
                Collections.singletonList("default"), Locale.UK, "UTC", 3, -1, null);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        Assert.assertEquals("Must specify a handover message when specifying a handover threshold", result.getStatus().getInfo());
    }

    @Test
    public void testGetSingle_Valid() throws DatabaseException {
        when(this.fakeDatabaseAi.getAIWithStatus(any(), any(), any(JsonSerializer.class))).thenReturn(TestDataHelper.getSampleAI());
        ApiResult result = this.aiLogic.getSingleAI(VALIDDEVID, AIID, null);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_Valid_Return() throws DatabaseException {
        when(this.fakeDatabaseAi.getAIWithStatus(any(), any(), any(JsonSerializer.class))).thenReturn(TestDataHelper.getSampleAI());
        ApiAi result = (ApiAi) this.aiLogic.getSingleAI(VALIDDEVID, AIID, null);
        Assert.assertEquals(AIID.toString(), result.getAiid());
    }

    /**
     * Common setup code for UI-status tests
     *
     * @param trainingStatus
     * @param hasLinked
     * @return
     * @throws DatabaseException
     */

    public ApiAi getSingleUIFields(TrainingStatus trainingStatus, boolean hasLinked) throws DatabaseException {
        ApiAi ai = TestDataHelper.getAi(TestDataHelper.getBackendStatus(trainingStatus));
        when(this.fakeDatabaseAi.getAIWithStatus(any(), any(), any(JsonSerializer.class))).thenReturn(ai);
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any(), any())).thenReturn(
                hasLinked ? Collections.singletonList(TestDataHelper.getAiBot(1, "name"))
                        : Collections.EMPTY_LIST);
        return (ApiAi) this.aiLogic.getSingleAI(VALIDDEVID, AIID, null);
    }

    @Test
    // no training, no linked bots so we cant chat
    public void testGetSingle_UI_empty() throws DatabaseException {
        ApiAi result = getSingleUIFields(TrainingStatus.AI_UNDEFINED, false);
        Assert.assertEquals(UITrainingState.Status.empty, result.getUiTrainingState().getUiTrainingStatus());
        Assert.assertFalse(result.isCanChat());
    }

    @Test
    // emb is complete so we can chat
    public void testGetSingle_UI_training() throws DatabaseException {
        ApiAi result = getSingleUIFields(TrainingStatus.AI_TRAINING_COMPLETE, false);
        Assert.assertEquals(UITrainingState.Status.completed, result.getUiTrainingState().getUiTrainingStatus());
        Assert.assertTrue(result.isCanChat());
    }

    @Test
    // no training but linked bots means we can chat
    public void testGetSingle_UI_linked() throws DatabaseException {
        ApiAi result = getSingleUIFields(TrainingStatus.AI_UNDEFINED, true);
        Assert.assertEquals(UITrainingState.Status.empty, result.getUiTrainingState().getUiTrainingStatus());
        Assert.assertTrue(result.isCanChat());
    }

    @Test
    // error in emb training so we cant chat
    public void testGetSingle_UI_errors() throws DatabaseException {
        ApiAi result = getSingleUIFields(TrainingStatus.AI_ERROR, false);
        Assert.assertEquals(UITrainingState.Status.error, result.getUiTrainingState().getUiTrainingStatus());
        Assert.assertTrue(result.getUiTrainingState().getErrorMessage().length() > 0);
        Assert.assertFalse(result.isCanChat());
    }

    @Test
    public void testGetSingle_DBFail_Error() throws DatabaseException {
        when(this.fakeDatabaseAi.getAIWithStatus(any(), any(), any(JsonSerializer.class))).thenThrow(DatabaseException.class);
        ApiResult result = this.aiLogic.getSingleAI(VALIDDEVID, AIID, null);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetSingle_DB_NotFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getAIWithStatus(any(), any(), any(JsonSerializer.class))).thenReturn(null);
        ApiResult result = this.aiLogic.getSingleAI(VALIDDEVID, AIID, null);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid() throws DatabaseException {
        ArrayList<Pair<ApiAi, String>> returnList = getAIList();
        when(this.fakeDatabaseAi.getAllAIs(eq(VALIDDEVID), any())).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(VALIDDEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetAll_Valid_Return() throws DatabaseException {
        ArrayList<Pair<ApiAi, String>> returnList = getAIList();
        when(this.fakeDatabaseAi.getAllAIs(eq(VALIDDEVID), any())).thenReturn(returnList);
        ApiResult result = this.aiLogic.getAIs(VALIDDEVID);
        Assert.assertTrue(result instanceof ApiAiList);
        ApiAiList list = (ApiAiList) result;
        Assert.assertNotNull(list.getAiList());
        Assert.assertFalse(list.getAiList().isEmpty());
        Assert.assertEquals(AIID.toString(), list.getAiList().get(0).getAiid());
    }

    @Test
    public void testGetAll_NoneFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getAllAIs(eq(VALIDDEVID), any())).thenReturn(new ArrayList<>());
        ApiResult result = this.aiLogic.getAIs(VALIDDEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        ApiAiList list = (ApiAiList) result;
        Assert.assertNotNull(list.getAiList());
        Assert.assertTrue(list.getAiList().isEmpty());
    }

    @Test
    public void testGetAll_DBFail() throws DatabaseException {
        ArrayList<Pair<ApiAi, String>> returnList = getAIList();
        when(this.fakeDatabaseAi.getAllAIs(any(), any())).thenThrow(DatabaseException.class);
        this.aiLogic.getAIs(VALIDDEVID);
        ApiResult result = this.aiLogic.getAIs(VALIDDEVID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDelete_Valid() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenReturn(null);
        when(this.fakeDatabaseAi.deleteAi(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        // Make sure we request it to be deleted by the backends
        verify(this.fakeAiServices).deleteAI(any(), any());
    }

    @Test
    public void testDelete_DBFail_NotFound() throws DatabaseException {
        when(this.fakeDatabaseAi.deleteAi(any(), any())).thenReturn(false);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testDelete_AI_readonly() throws DatabaseException {
        setupAiReadonlyMode(this.fakeDatabaseAi);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testDelete_servicesException_notFound() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseAi.deleteAi(any(), any())).thenReturn(true);
        ServerConnector.AiServicesException exMain = new ServerConnector.AiServicesException("main");
        ServerConnector.AiServicesException exSub = new ServerConnector.AiServicesException("sub", HttpURLConnection.HTTP_NOT_FOUND);
        exMain.addSuppressed(exSub);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        doThrow(exMain).when(this.fakeAiServices).deleteAI(any(), any());
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testDelete_servicesException_other() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseAi.deleteAi(any(), any())).thenReturn(true);
        ServerConnector.AiServicesException exMain = new ServerConnector.AiServicesException("main");
        ServerConnector.AiServicesException exSub = new ServerConnector.AiServicesException("sub", HttpURLConnection.HTTP_INTERNAL_ERROR);
        exMain.addSuppressed(exSub);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        doThrow(exMain).when(this.fakeAiServices).deleteAI(any(), any());
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testDelete_withPublishedBot_botNotPurchased_canBeDeleted() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseAi.deleteAi(any(), any())).thenReturn(true);
        when(this.fakeDatabaseMarketplace.hasBotBeenPurchased(anyInt())).thenReturn(false);
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeDatabaseAi).deleteAi(any(), any());
        verify(this.fakeAiServices).deleteAI(any(), any());
    }

    @Test
    public void testDelete_withPublishedBot_botPurchased_cannotBeDeleted() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.hasBotBeenPurchased(anyInt())).thenReturn(true);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        // Verify we don't actually request it to be deleted by the backends
        verify(this.fakeAiServices, never()).deleteAI(any(), any());
        verify(this.fakeDatabaseAi, never()).deleteAi(any(), any());
    }

    @Test
    public void testDelete_DBFail_Error() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.deleteAi(any(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiLogic.deleteAI(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        when(this.fakeDatabaseAi.updateAI(any(UUID.class), any(ApiAi.class), any(JsonSerializer.class))).thenReturn(true);
        ApiResult result = callDefaultUpdateAI();
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_dbFail() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        when(this.fakeDatabaseAi.updateAI(any(UUID.class), any(ApiAi.class), any(JsonSerializer.class))).thenReturn(false);
        ApiResult result = callDefaultUpdateAI();
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_AI_notFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = callDefaultUpdateAI();
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_AI_readonly() throws DatabaseException {
        setupAiReadonlyMode(this.fakeDatabaseAi);
        ApiResult result = callDefaultUpdateAI();
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAi_dbException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAI());
        when(this.fakeDatabaseAi.updateAI(any(UUID.class), any(ApiAi.class), any(JsonSerializer.class)))
                .thenThrow(DatabaseException.class);
        ApiResult result = callDefaultUpdateAI();
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBots_valid() throws DatabaseException {
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiAiBotList result = (ApiAiBotList) this.aiLogic.getLinkedBots(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(1, result.getBotList().size());
        Assert.assertEquals(SAMPLEBOT.getBotId(), result.getBotList().get(0).getBotId());
    }

    @Test
    public void testGetLinkedBots_DBException() throws DatabaseException {
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiLogic.getLinkedBots(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBots_noResults() throws DatabaseException {
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        ApiAiBotList result = (ApiAiBotList) this.aiLogic.getLinkedBots(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertTrue(result.getBotList().isEmpty());
    }

    @Test
    public void testLinkBotToAi() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTraining(null, AI_IDENTITY);
    }

    @Test
    public void testLinkBotToAi_trainingInProgress() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTrainingIfNeeded(AI_IDENTITY);
    }

    @Test
    public void testLinkBotToAi_DB_failed_update() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(false);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_AI_notFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_AI_readonly() throws DatabaseException {
        setupAiReadonlyMode(this.fakeDatabaseAi);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_DBException() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenThrow(DatabaseException.class);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_aiTrainingInProgress_AIException()
            throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(true);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeAiServices).stopTraining(any(), any());
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botNotPurchased_notOwned() throws DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(UUID.randomUUID());
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botNotPurchased_Owned() throws DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(DEVID_UUID);
        bot.setPublishingType(AiBot.PublishingType.SKILL);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.emptyList());
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botAlreadyLinked() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(SAMPLEBOT);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_cannotLinkToTemplate() throws DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setPublishingType(AiBot.PublishingType.TEMPLATE);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(Collections.singletonList(SAMPLEBOT));
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_botNotFound() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(null);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testLinkBotToAi_maximumBotsReached() throws DatabaseException {
        final int maxLinkedBots = 5;
        List<AiBot> bots = generateBots(maxLinkedBots);
        AiBot botToLink = generateSkillToLink(99);
        verifyCannotLinkMoreBots(bots, botToLink, maxLinkedBots);
    }

    @Test
    public void testLinkBotToAi_maximumBotsReached_resetAfterUnlinking() throws DatabaseException {
        final int maxLinkedBots = 5;
        List<AiBot> bots = generateBots(maxLinkedBots);
        AiBot botToLink = generateSkillToLink(99);
        verifyCannotLinkMoreBots(bots, botToLink, maxLinkedBots);

        // now "unlink" a bot
        bots.remove(0);
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(bots);
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, botToLink.getBotId());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi() throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTraining(null, AI_IDENTITY);
    }

    @Test
    public void testUnlinkBotFromAi_aiTrainingInProgress() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeAiServices, never()).stopTrainingIfNeeded(AI_IDENTITY);
    }

    @Test
    public void testUnlinkBotFromAi_AI_notFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_AI_readonly() throws DatabaseException {
        setupAiReadonlyMode(this.fakeDatabaseAi);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_DB_failed_update() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt())).thenReturn(false);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_DBException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingCompleted()));
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUnlinkBotFromAi_aiTrainingFile_AIException()
            throws DatabaseException, ServerConnector.AiServicesException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getAi(TestDataHelper.getTrainingInProgress()));
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(true);
        doThrow(ServerConnector.AiServicesException.class).when(this.fakeAiServices).stopTraining(any(), any());
        ApiResult result = this.aiLogic.unlinkBotFromAI(DEVID_UUID, AIID, BOTID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }


    @Test
    public void testGetPublishedBotForAI_hasBot() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenReturn(SAMPLEBOT);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBotForAI_hasNoPublishedBot() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetPublishedBotForAI_DBException() throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiLogic.getPublishedBotForAI(DEVID_UUID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testExportBot() throws DatabaseException {
        ApiIntent intent = new ApiIntent("intent_name", "topic_in", "topic_out");
        intent.setWebHook(new WebHook(AIID, "intent_name", "http://not.a.real.address:8080", true));
        intent.setUserSays(Collections.singletonList("test"));
        intent.setResponses(Collections.singletonList("ok"));

        ApiBotStructure result = setupBotExport(intent);
        BotStructure bot = result.getBotStructure();

        Assert.assertEquals(TestDataHelper.getSampleAI().getName(), bot.getName());
        Assert.assertEquals(TestDataHelper.getSampleAI().getDescription(), bot.getDescription());
        Assert.assertEquals(intent.getIntentName(), bot.getIntents().get(0).getIntentName());
        Assert.assertEquals(intent.getWebHook(), bot.getIntents().get(0).getWebHook());
    }

    @Test
    public void testExportBot_noWebhookIfNotPresent() throws DatabaseException {
        ApiIntent intent = new ApiIntent("intent_name", "topic_in", "topic_out");
        intent.setUserSays(Collections.singletonList("test"));
        intent.setResponses(Collections.singletonList("ok"));

        ApiBotStructure result = setupBotExport(intent);
        BotStructure bot = result.getBotStructure();

        Assert.assertEquals(TestDataHelper.getSampleAI().getName(), bot.getName());
        Assert.assertEquals(TestDataHelper.getSampleAI().getDescription(), bot.getDescription());
        Assert.assertEquals(intent.getIntentName(), bot.getIntents().get(0).getIntentName());
        Assert.assertNull(bot.getIntents().get(0).getWebHook());
    }

    @Test
    public void testExportBot_databaseException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiLogic.exportBotData(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testExportBot_DoesntExist() {
        ApiResult result = this.aiLogic.exportBotData(VALIDDEVID, AIID);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testImportBot() {
        ApiResult result = this.aiLogic.importBot(VALIDDEVID, null);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_invalidVersion() {
        AiBotConfig config = this.generateAiBotConfig();
        config.setVersion(9999);
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_badAiLookup() throws DatabaseException {
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(false);
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_badBotLookup() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getIsBotLinkedToAi(any(), any(), anyInt())).thenReturn(null);
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }


    @Test
    public void testSetBotConfig_dbFail() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getIsBotLinkedToAi(any(), any(), anyInt())).thenReturn(generateLinkedBotIds());
        when(this.fakeDatabaseAi.setAiBotConfig(any(), any(), anyInt(), any(), any())).thenReturn(false);
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_success() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getIsBotLinkedToAi(any(), any(), anyInt())).thenReturn(generateLinkedBotIds());
        when(this.fakeDatabaseAi.setAiBotConfig(any(), any(), anyInt(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getBotConfigDefinition(any(), any(), any())).thenReturn(this.generateAiBotConfigDefinition());
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_success_bot0() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.setAiBotConfig(any(), any(), anyInt(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getBotConfigDefinition(any(), any(), any())).thenReturn(this.generateAiBotConfigDefinition());
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 0, config);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_failureToSaveToDb() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getIsBotLinkedToAi(any(), any(), anyInt())).thenReturn(generateLinkedBotIds());
        when(this.fakeDatabaseAi.setAiBotConfig(any(), any(), anyInt(), any(), any())).thenReturn(false);
        when(this.fakeDatabaseAi.getBotConfigDefinition(any(), any(), any())).thenReturn(this.generateAiBotConfigDefinition());
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_DB_exception() throws DatabaseException {
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenThrow(DatabaseException.class);
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_AI_notFound() throws DatabaseException {
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testSetBotConfig_AI_readonly() throws DatabaseException {
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        setupAiReadonlyMode(this.fakeDatabaseAi);
        AiBotConfig config = this.generateAiBotConfig();
        ApiResult result = this.aiLogic.setAiBotConfig(DEVID_UUID, AIID, 1, config);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSetAiBotConfiguration_failApiSet() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.setAiBotConfig(any(), any(), anyInt(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.setBotConfigDefinition(any(), any(), any(), any())).thenReturn(false);
        AiBotConfigWithDefinition def = this.generateAiBotConfigWithDefinition();
        ApiResult result = this.aiLogic.setAiBotConfigDescription(DEVID_UUID, AIID, def);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSetAiBotConfiguration_invalidConfig() {
        AiBotConfig config = new AiBotConfig(new HashMap<String, String>() {{
            put("a", "b");
        }});
        AiBotConfigWithDefinition def = new AiBotConfigWithDefinition(config, null);
        ApiResult result = this.aiLogic.setAiBotConfigDescription(DEVID_UUID, AIID, def);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSetAiBotConfiguration_success() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.checkAIBelongsToDevId(any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.setAiBotConfig(any(), any(), anyInt(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.setBotConfigDefinition(any(), any(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getBotConfigDefinition(any(), any(), any())).thenReturn(this.generateAiBotConfigDefinition());
        AiBotConfigWithDefinition def = this.generateAiBotConfigWithDefinition();
        ApiResult result = this.aiLogic.setAiBotConfigDescription(DEVID_UUID, AIID, def);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testSetAiBotConfiguration_AI_notFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        AiBotConfigWithDefinition def = this.generateAiBotConfigWithDefinition();
        ApiResult result = this.aiLogic.setAiBotConfigDescription(DEVID_UUID, AIID, def);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testSetAiBotConfiguration_AI_readonly() throws DatabaseException {
        setupAiReadonlyMode(this.fakeDatabaseAi);
        AiBotConfigWithDefinition def = this.generateAiBotConfigWithDefinition();
        ApiResult result = this.aiLogic.setAiBotConfigDescription(DEVID_UUID, AIID, def);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testSetAiBotConfiguration_DB_exception() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenThrow(DatabaseException.class);
        AiBotConfigWithDefinition def = this.generateAiBotConfigWithDefinition();
        ApiResult result = this.aiLogic.setAiBotConfigDescription(DEVID_UUID, AIID, def);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBotData_notFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getLinkedBotData(any(), any(), anyInt(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.getLinkedBotData(DEVID_UUID, AIID, 1);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBotData_success() throws DatabaseException {
        ApiLinkedBotData linkedData = new ApiLinkedBotData(TestBotHelper.SAMPLEBOT, generateAiBotConfig());
        when(this.fakeDatabaseAi.getLinkedBotData(any(), any(), anyInt(), any())).thenReturn(linkedData);
        ApiResult result = this.aiLogic.getLinkedBotData(DEVID_UUID, AIID, 1);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testGetLinkedBotData_DB_exception() throws DatabaseException {
        when(this.fakeDatabaseAi.getLinkedBotData(any(), any(), anyInt(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiLogic.getLinkedBotData(DEVID_UUID, AIID, 1);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    private Pair<ApiAi, ApiResult> cloneBotCommon(final ApiAi baseAi, final String passthroughUrl) throws DatabaseException {
        final AiBot originalBot = new AiBot(DEVID_UUID, UUID.fromString(baseAi.getAiid()), 12345, baseAi.getName(), baseAi.getDescription(),
                "long description", "alert", "badge", BigDecimal.ZERO, "sample", "category", "license", DateTime.now(), "privacy",
                "classif", "1.0", "videolink", AiBot.PublishingState.NOT_PUBLISHED, AiBot.PublishingType.SKILL, "icon");
        final String trainingFile = "aaaaa\nbbbbb";
        final UUID generatedAiid = UUID.randomUUID();
        final String newName = "this is a new name";
        final String newDescription = "new description";
        final boolean newIsPrivate = true;
        final int newPersonality = 3;
        final double newConfidence = 0.3;
        final int newVoice = 9;
        final Locale newLanguage = Locale.ITALY;
        final String newTimezone = "Europe/Dublin";
        final List<String> newDefaultResponses = Collections.singletonList("New default response");
        final String newPassthroughUrl = passthroughUrl;

        ApiAi importedAi = new ApiAi(generatedAiid.toString(), "token", newName, newDescription, DateTime.now(),
                newIsPrivate, baseAi.getBackendStatus(), baseAi.trainingFileUploaded(), newPersonality,
                newConfidence, newVoice, newLanguage, newTimezone, null,
                newPassthroughUrl, newDefaultResponses, null);

        // For when we read the original AI for export
        when(this.fakeDatabaseAi.getAI(any(), eq(UUID.fromString(baseAi.getAiid())), any())).thenReturn(baseAi);
        // For when we read the imported AI
        when(this.fakeDatabaseAi.getAI(any(), any(), any(JsonSerializer.class), any())).thenReturn(importedAi);
        // For when we read it one last time to return it to the caller
        when(this.fakeDatabaseAi.getAI(any(), eq(generatedAiid), any())).thenReturn(importedAi);
        when(this.fakeDatabaseAi.getAIWithStatusForEngineVersion(any(UUID.class), eq(generatedAiid), anyString(), any(JsonSerializer.class), any(DatabaseTransaction.class))).thenReturn(importedAi);
        when(this.fakeDatabaseAi.getAIWithStatusForEngineVersion(any(UUID.class), eq(generatedAiid), anyString(), any(JsonSerializer.class))).thenReturn(importedAi);

        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(trainingFile);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(generatedAiid);
        when(this.fakeDatabaseAi.updatePassthroughUrl(any(), any(), anyString(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.updateDefaultChatResponses(any(), any(), any(), any(), any())).thenReturn(true);
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn("");
        TestDataHelper.mockDatabaseCreateAIInTrans(this.fakeDatabaseAi, generatedAiid);

        AILogic spyLogic = spy(this.aiLogic);
        ApiResult cloned = spyLogic.cloneBot(originalBot.getDevId(), originalBot.getAiid(),
                newName, newDescription, newIsPrivate, newPersonality, newConfidence, newVoice, newLanguage,
                newTimezone, newDefaultResponses, newPassthroughUrl);

        return new Pair<>(importedAi, cloned);
    }

    @Test
    public void testCloneBot_no_passthrough() throws DatabaseException {
        final ApiAi baseAi = TestDataHelper.getSampleAI();

        Pair<ApiAi, ApiResult> aiPair = this.cloneBotCommon(baseAi, "");

        ApiAi importedAi = aiPair.getA();
        ApiAi cloned = (ApiAi) aiPair.getB();

        Assert.assertNotEquals(baseAi.getAiid(), cloned.getAiid());
        Assert.assertEquals(importedAi.getName(), cloned.getName());
        Assert.assertEquals(importedAi.getDescription(), cloned.getDescription());
        Assert.assertEquals(importedAi.getTimezone(), cloned.getTimezone());
        Assert.assertEquals(importedAi.getLanguage(), cloned.getLanguage());
        Assert.assertEquals(importedAi.getVoice(), cloned.getVoice());
        Assert.assertEquals(importedAi.getPersonality(), cloned.getPersonality());

        Assert.assertEquals(importedAi.getPassthroughUrl(), cloned.getPassthroughUrl());
        Assert.assertEquals(importedAi.getDefaultChatResponses(), cloned.getDefaultChatResponses());
    }

    @Test
    public void testCloneBot_passthrough_rejected() throws DatabaseException {
        final ApiAi baseAi = TestDataHelper.getSampleAI();


        Pair<ApiAi, ApiResult> aiPair = this.cloneBotCommon(baseAi, "some_passthrough_url");

        ApiError error = (ApiError) aiPair.getB();

        Assert.assertEquals(error.getStatus().getCode(), 400);
    }

    @Test
    public void testCloneBot_passthrough_allowed_if_feature_toggled() throws DatabaseException {

        final ApiAi baseAi = TestDataHelper.getSampleAI();
        when(this.fakeFeatureToggler.getStateforDev(any(), eq("enable-passthrough-url"))).thenReturn(FeatureToggler.FeatureState.T1);

        Pair<ApiAi, ApiResult> aiPair = this.cloneBotCommon(baseAi, "some_passthrough_url");

        ApiAi importedAi = aiPair.getA();
        ApiAi cloned = (ApiAi) aiPair.getB();

        Assert.assertNotEquals(baseAi.getAiid(), cloned.getAiid());
        Assert.assertEquals(importedAi.getName(), cloned.getName());
        Assert.assertEquals(importedAi.getDescription(), cloned.getDescription());
        Assert.assertEquals(importedAi.getTimezone(), cloned.getTimezone());
        Assert.assertEquals(importedAi.getLanguage(), cloned.getLanguage());
        Assert.assertEquals(importedAi.getVoice(), cloned.getVoice());
        Assert.assertEquals(importedAi.getPersonality(), cloned.getPersonality());

        Assert.assertEquals(importedAi.getPassthroughUrl(), cloned.getPassthroughUrl());
        Assert.assertEquals(importedAi.getDefaultChatResponses(), cloned.getDefaultChatResponses());
    }

    @Test
    public void testCloneBot_cloneTemplate_templateOwned() throws DatabaseException {
        final UUID generatedAiid = UUID.randomUUID();

        ApiAi result = (ApiAi) testCloneBot_template(true, generatedAiid);

        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, result.getStatus().getCode());
        Assert.assertEquals(generatedAiid.toString(), result.getAiid());
    }

    @Test
    public void testCloneBot_cloneTemplate_templateNotOwned() throws DatabaseException {
        final UUID generatedAiid = UUID.randomUUID();

        ApiResult result = testCloneBot_template(false, generatedAiid);

        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    private ApiResult testCloneBot_template(final boolean owned, final UUID generatedAiid) throws DatabaseException {
        final UUID botToCloneOwner = UUID.randomUUID();
        final UUID clonerDevId = UUID.randomUUID();
        final ApiAi baseAi = TestDataHelper.getSampleAI();
        final AiBot originalBot = new AiBot(botToCloneOwner, UUID.fromString(baseAi.getAiid()), 12345, baseAi.getName(), baseAi.getDescription(),
                "long description", "alert", "badge", BigDecimal.ZERO, "sample", "category", "license", DateTime.now(), "privacy",
                "classif", "1.0", "videolink", AiBot.PublishingState.PUBLISHED, AiBot.PublishingType.TEMPLATE, "icon");

        final ApiAi clonedAi = new ApiAi(baseAi);
        clonedAi.setAiid(generatedAiid);
        when(this.fakeTools.createNewRandomUUID()).thenReturn(generatedAiid);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(clonerDevId)).thenReturn(
                owned ? Collections.singletonList(originalBot) : Collections.emptyList());

        // For when we read the original AI for export
        if (owned) {
            when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(baseAi).thenReturn(clonedAi);
        }

        when(this.fakeDatabaseAi.getAIWithStatusForEngineVersion(any(UUID.class), any(UUID.class), anyString(), any(JsonSerializer.class), any(DatabaseTransaction.class))).thenReturn(clonedAi);
        when(this.fakeDatabaseAi.getAIWithStatusForEngineVersion(any(UUID.class), any(UUID.class), anyString(), any(JsonSerializer.class))).thenReturn(clonedAi);
        when(this.fakeDatabaseAi.getAI(any(), any(), any(JsonSerializer.class), any())).thenReturn(clonedAi);
        TestDataHelper.mockDatabaseCreateAIInTrans(this.fakeDatabaseAi, generatedAiid);
        when(this.fakeDatabaseAi.updatePassthroughUrl(any(), any(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.updateDefaultChatResponses(any(), any(), any(), any(), any())).thenReturn(true);
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn("");

        return this.aiLogic.cloneBot(clonerDevId, originalBot.getAiid(), baseAi.getName(), baseAi.getDescription(),
                baseAi.getIsPrivate(), baseAi.getPersonality(), baseAi.getConfidence(), baseAi.getVoice(), baseAi.getLanguage(),
                baseAi.getTimezone(), baseAi.getDefaultChatResponses(), baseAi.getPassthroughUrl());
    }

    @Test
    public void testCloneBot_errorExportingOrImporting() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.cloneBot(DEVID_UUID, AIID, null, null, true, 1, 0.1, 0,
                Locale.ITALY, null, null, null);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testGenerateBotNameRandomSuffix() {
        String suffix = AILogic.generateBotNameRandomSuffix();
        Assert.assertEquals(6, suffix.length());
        Assert.assertEquals('_', suffix.charAt(0));
        Assert.assertEquals(suffix.toLowerCase(), suffix);
        Assert.assertNotEquals(suffix, AILogic.generateBotNameRandomSuffix());
    }

    @Test
    public void testUpdateLinkedBots() throws DatabaseException {
        List<AiBot> ownedBots = generateBotsWithIds(Arrays.asList(1, 2));
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.getBotsLinkedToAi(DEVID_UUID, AIID)).thenReturn(ownedBots);
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(true);
        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Collections.singletonList(ownedBots.get(1).getBotId()));
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeTransaction).commit();
    }

    @Test
    public void testUpdateLinkedBots_duplicatedInList() throws DatabaseException {
        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Arrays.asList(1, 2, 3, 2));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeTransaction, never()).commit();
    }

    @Test
    public void testUpdateLinkedBots_AI_notFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Arrays.asList(1, 2));
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
        verify(this.fakeTransaction, never()).commit();
    }

    @Test
    public void testUpdateLinkedBots_AI_readonly() throws DatabaseException {
        ApiAi ai = new ApiAi(getSampleAI());
        ai.setReadOnly(true);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(ai);
        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Arrays.asList(1, 2));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeTransaction, never()).commit();
    }

    @Test
    public void testUpdateLinkedBots_overLimitLinks() throws DatabaseException {
        List<AiBot> ownedBots = generateBotsWithIds(Arrays.asList(1, 2, 3, 4, 5));
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.getBotsLinkedToAi(DEVID_UUID, AIID)).thenReturn(ownedBots);
        // Remove bot 2 and add 6 and 7
        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Arrays.asList(1, 3, 4, 5, 6, 7));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeDatabaseAi, never()).linkBotToAi(any(), any(), anyInt(), any());
        verify(this.fakeTransaction, never()).commit();
    }

    @Test
    public void testUpdateLinkedBots_DBException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Arrays.asList(1, 2));
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
        verify(this.fakeTransaction, never()).commit();
    }

    @Test
    public void testUpdateLinkedBots_botNotOwned() throws DatabaseException {
        AiBot bot = new AiBot(SAMPLEBOT);
        bot.setDevId(UUID.randomUUID());
        List<AiBot> ownedBots = generateBotsWithIds(Arrays.asList(1, 2));
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.getBotsLinkedToAi(DEVID_UUID, AIID)).thenReturn(ownedBots);
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(bot);
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(true);
        // 99 is not owned so it cannot be linked
        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Arrays.asList(1, 99));
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
        verify(this.fakeTransaction, never()).commit();
        verify(this.fakeTransaction).rollback();
    }

    @Test
    public void testUpdateLinkedBots_unlinkBot_notLinked() throws DatabaseException {
        List<AiBot> ownedBots = generateBotsWithIds(Arrays.asList(1, 2));
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.getBotsLinkedToAi(DEVID_UUID, AIID)).thenReturn(ownedBots);
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(false);
        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Collections.singletonList(ownedBots.get(1).getBotId()));
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
        verify(this.fakeTransaction, never()).commit();
        verify(this.fakeTransaction).rollback();
    }

    @Test
    public void testUpdateLinkedBots_removeAll() throws DatabaseException {
        List<AiBot> ownedBots = generateBotsWithIds(Arrays.asList(1, 2));
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.getBotsLinkedToAi(DEVID_UUID, AIID)).thenReturn(ownedBots);
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(true);

        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, Collections.emptyList());

        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeTransaction).commit();
        verify(this.fakeDatabaseAi, times(1))
                .unlinkBotFromAi(DEVID_UUID, AIID, ownedBots.get(0).getBotId(), this.fakeTransaction);
        verify(this.fakeDatabaseAi, times(1))
                .unlinkBotFromAi(DEVID_UUID, AIID, ownedBots.get(1).getBotId(), this.fakeTransaction);
    }

    @Test
    public void testUpdateLinkedBots_removAllAddAllNew() throws DatabaseException {
        List<AiBot> ownedBots = generateBotsWithIds(Arrays.asList(1, 2, 3, 4, 5));
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.unlinkBotFromAi(any(), any(), anyInt(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.getBotsLinkedToAi(DEVID_UUID, AIID)).thenReturn(ownedBots); // For unlinking only
        when(this.fakeDatabaseAi.linkBotToAi(any(), any(), anyInt(), any())).thenReturn(true);

        List<Integer> newBotIds = Arrays.asList(10, 11, 22, 13, 14);
        List<AiBot> newBots = generateBotsWithIds(newBotIds);
        for (int i = 0; i < newBotIds.size(); i++) {
            newBots.get(i).setPublishingType(AiBot.PublishingType.SKILL);
            when(this.fakeDatabaseMarketplace.getBotDetails(newBotIds.get(i))).thenReturn(newBots.get(i));
        }

        ApiResult result = this.aiLogic.updateLinkedBots(DEVID_UUID, AIID, newBotIds);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeTransaction).commit();
        for (int i = 0; i < ownedBots.size(); i++) {
            verify(this.fakeDatabaseAi, times(1))
                    .unlinkBotFromAi(DEVID_UUID, AIID, ownedBots.get(0).getBotId(), this.fakeTransaction);
        }
        for (int i = 0; i < newBotIds.size(); i++) {
            verify(this.fakeDatabaseAi, times(1))
                    .linkBotToAi(DEVID_UUID, AIID, newBotIds.get(0), this.fakeTransaction);
        }
    }

    @Test
    public void testCreateImportedBot() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBot_language_fallback()
            throws AILogic.BotImportException, ParameterValidationException, DatabaseException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        botStructure.setLanguage("NOT A REAL LANGUAGE");
        when(this.fakeValidate.validateLocale(anyString(), anyString())).thenThrow(ParameterValidationException.class);
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
        ArgumentCaptor<Locale> localeArg = ArgumentCaptor.forClass(Locale.class);
        verify(this.fakeDatabaseAi).createAI(any(), anyString(), anyString(), any(), anyBoolean(), anyString(),
                localeArg.capture(), anyString(), anyDouble(), anyInt(), anyInt(), any(), anyInt(), anyInt(), any(), any(), any());
        Assert.assertEquals(AILogic.DEFAULT_LOCALE, localeArg.getValue());
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_createAi_genericException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        when(this.fakeDatabaseAi.createAI(any(), anyString(), anyString(), any(), anyBoolean(), anyString(),
                any(), anyString(), anyDouble(), anyInt(), anyInt(), any(), anyInt(), anyInt(), any(), any(), any())).thenThrow(DatabaseException.class);
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_getAi_genericException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        when(this.fakeDatabaseAi.getAIWithStatusForEngineVersion(any(UUID.class), any(UUID.class), anyString(), any(JsonSerializer.class), any(DatabaseTransaction.class))).thenThrow(DatabaseException.class);
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_entities_dbException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        when(this.fakeDatabaseEntitiesIntents.getEntities(any())).thenThrow(DatabaseException.class);
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBot_entities_differentSize() throws AILogic.BotImportException, DatabaseException {
        final String entityName = "entity";
        List<String> existingValues = Arrays.asList("value1", "value2", "value3");
        List<String> newValues = Arrays.asList("value4", "value5");
        BotStructure botToImport = setupImportEntityValueTests(entityName, existingValues, newValues);

        expectedException.expect(AILogic.BotImportUserException.class);
        expectedException.expectMessage("Entity entity already exists and has different number of values");
        this.aiLogic.createImportedBot(VALIDDEVID, botToImport);
    }

    @Test
    public void testCreateImportedBot_entities_differentValues() throws AILogic.BotImportException, DatabaseException {
        final String entityName = "entity";
        List<String> existingValues = Arrays.asList("value1", "value2");
        List<String> newValues = Arrays.asList(existingValues.get(0), "value3");
        BotStructure botToImport = setupImportEntityValueTests(entityName, existingValues, newValues);

        expectedException.expect(AILogic.BotImportUserException.class);
        expectedException.expectMessage("Entity entity already exists and has different set of values");
        this.aiLogic.createImportedBot(VALIDDEVID, botToImport);
    }

    @Test
    public void testCreateImportedBot_handover() throws AILogic.BotImportException, DatabaseException {
        String handoverMessage = "handover_message";
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        botStructure.setHandoverMessage("message");
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_handoverTimeoutRequiresMessage() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        botStructure.setErrorThresholdHandover(1);
        botStructure.setHandoverResetTimeoutSeconds(1);
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_writeEntities_dbException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        doThrow(DatabaseException.class).when(this.fakeDatabaseEntitiesIntents).writeEntity(any(), anyString(), any(), any());
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_writeIntent_dbException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        doThrow(DatabaseException.class).when(this.fakeDatabaseEntitiesIntents).writeIntent(any(), any(), anyString(), any(), any());
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_writeIntent_webhook_dbException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        when(this.fakeDatabaseEntitiesIntents.createWebHook(any(), anyString(), anyString(), anyBoolean(), any())).thenReturn(false);
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_updateTrainingFile_dbException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        when(this.fakeDatabaseAi.updateAiTrainingFile(any(), anyString(), any())).thenThrow(DatabaseException.class);
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_commit_dbException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        doThrow(DatabaseException.class).when(this.fakeTransaction).commit();
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_updatePassthroughUrl_dbException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        when(this.fakeDatabaseAi.updatePassthroughUrl(any(), any(), anyString(), any())).thenReturn(false);
        BotStructure botStructure = getBotstructure();
        botStructure.setPassthroughUrl("http://my.url");
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_updateDefaultResponses_dbException() throws AILogic.BotImportException, DatabaseException {
        setupFakeImport();
        when(this.fakeDatabaseAi.updateDefaultChatResponses(any(), any(), any(), any(), any())).thenReturn(false);
        BotStructure botStructure = getBotstructure();
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBot_linkedBots_nonExisting() throws DatabaseException, AILogic.BotImportException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        final int botId = 123;
        botStructure.setLinkedSkills(Collections.singletonList(botId));
        expectedException.expect(AILogic.BotImportException.class);
        expectedException.expectMessage(String.format(AILogic.LINK_BOT_NOT_EXIST_TEMPLATE, botId));
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBot_linkedBots_notOwned() throws DatabaseException, AILogic.BotImportException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        final int botId = 123;
        final String botName = "skill1";
        botStructure.setLinkedSkills(Collections.singletonList(botId));
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(getAiBot(botId, botName));
        expectedException.expect(AILogic.BotImportException.class);
        expectedException.expectMessage(String.format(AILogic.LINK_BOT_NOT_OWNED_TEMPLATE, botName, botId));
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBot_linkedBots() throws DatabaseException, AILogic.BotImportException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        final int botId = 123;
        final String botName = "skill1";
        botStructure.setLinkedSkills(Collections.singletonList(botId));
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenReturn(Arrays.asList(
                getAiBot(999, "other"), getAiBot(botId, botName)));
        when(this.fakeDatabaseMarketplace.getBotDetails(anyInt())).thenReturn(getAiBot(botId, botName));
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
        verify(this.fakeDatabaseAi).linkBotToAi(VALIDDEVID, AIID, botId, this.fakeTransaction);
    }

    @Test(expected = AILogic.BotImportException.class)
    public void testCreateImportedBot_linkedBots_getPurchasedBots_dbException() throws DatabaseException, AILogic.BotImportException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        final int botId = 123;
        botStructure.setLinkedSkills(Collections.singletonList(botId));
        when(this.fakeTools.createNewRandomUUID()).thenReturn(AIID);
        when(this.fakeDatabaseMarketplace.getPurchasedBots(any())).thenThrow(DatabaseException.class);
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBot_createAi_nameClash_errorSurfaced() throws DatabaseException, AILogic.BotImportException {
        setupFakeImport();
        // Make createAI returning AIID not match the existing one to cause the "name clash" error
        when(this.fakeTools.createNewRandomUUID()).thenReturn(UUID.randomUUID());
        BotStructure botStructure = getBotstructure();
        expectedException.expect(AILogic.BotImportException.class);
        expectedException.expectMessage("A bot with that name already exists");
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBot_fail_with_passthrough() throws DatabaseException, AILogic.BotImportException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        botStructure.setPassthroughUrl("passthrough_url");
        expectedException.expect(AILogic.BotImportException.class);
        expectedException.expectMessage("This bot uses passthrough URL, but this is not available for this DevId.");
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBot_pass_with_passthrough_toggle() throws DatabaseException, AILogic.BotImportException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        botStructure.setPassthroughUrl("passthrough_url");
        when(this.fakeFeatureToggler.getStateforDev(any(), eq("enable-passthrough-url"))).thenReturn(FeatureToggler.FeatureState.T1);
        this.aiLogic.createImportedBot(VALIDDEVID, botStructure);
    }

    @Test
    public void testCreateImportedBotInPlace() throws DatabaseException, ParameterValidationException {
        setupFakeImport();
        BotStructure botStructure = getBotstructure();
        botStructure.setDescription("newndescription");
        botStructure.setTimezone("newtimezone");
        botStructure.setLanguage("ca-ES");
        botStructure.setConfidence(0.999);
        botStructure.setPersonality(1234);
        botStructure.setVoice(4321);
        List<String> defaultResponses = Collections.singletonList("newdefresponse");
        botStructure.setDefaultResponses(defaultResponses);

        when(this.fakeValidate.validateLocale(any(), any())).thenReturn(Locale.forLanguageTag("ca-ES"));

        ApiAi result = (ApiAi) this.aiLogic.importBotInPlace(VALIDDEVID, AIID, botStructure);

        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        Assert.assertEquals(AIID.toString(), result.getAiid());
        ArgumentCaptor<ApiAi> argument = ArgumentCaptor.forClass(ApiAi.class);
        verify(this.fakeDatabaseAi).updateAI(
                eq(VALIDDEVID), argument.capture(),
                any(JsonSerializer.class),
                any(DatabaseTransaction.class));

        Assert.assertEquals("newndescription", argument.getValue().getDescription());
        Assert.assertEquals(Locale.forLanguageTag("ca-ES"), argument.getValue().getLanguage());
        Assert.assertEquals("newtimezone", argument.getValue().getTimezone());
        Assert.assertEquals(0.999, argument.getValue().getConfidence(), 0.00001);
        Assert.assertEquals(1234, argument.getValue().getPersonality());
        Assert.assertEquals(4321, argument.getValue().getVoice());
        Assert.assertEquals(defaultResponses, argument.getValue().getDefaultChatResponses());

    }

    @Test
    public void testCreateImportedBotInPlace_invalidOriginAiid() throws DatabaseException {
        when(this.fakeDatabaseAi.getAIWithStatus(any(), any(), any(), any())).thenReturn(null);
        BotStructure botStructure = getBotstructure();
        ApiResult result = this.aiLogic.importBotInPlace(VALIDDEVID, AIID, botStructure);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testCreateImportedBotInPlace_botAlreadyPublished() throws DatabaseException {
        BotStructure botStructure = getBotstructure();
        when(this.fakeDatabaseAi.getAI(any(), any(), any(JsonSerializer.class), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseMarketplace.getPublishedBotForAI(any(), any())).thenReturn(getAiBot(1, "published"));
        ApiResult result = this.aiLogic.importBotInPlace(VALIDDEVID, AIID, botStructure);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testCreateImportedBotInPlace_existingIntents_cleared() throws DatabaseException {
        ApiAi existingAi = getSampleAI();
        BotStructure botStructure = getBotstructure();
        List<String> existingIntentNames = Arrays.asList("previntent1", "previntent2");
        ApiIntentList existingIntents = new ApiIntentList(AIID, existingIntentNames);
        when(this.fakeDatabaseAi.getAI(any(), any(), any(JsonSerializer.class), any())).thenReturn(existingAi);
        when(this.fakeDatabaseEntitiesIntents.getIntentsDetails(any(), any())).thenReturn(existingIntents);

        this.aiLogic.importBotInPlace(VALIDDEVID, AIID, botStructure);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(this.fakeDatabaseEntitiesIntents, times(existingIntentNames.size()))
                .deleteIntent(eq(VALIDDEVID), eq(AIID), captor.capture(), any());
        Assert.assertEquals(existingIntentNames.size(), captor.getAllValues().size());
        Assert.assertEquals(existingIntentNames.get(0), captor.getAllValues().get(0));
        Assert.assertEquals(existingIntentNames.get(1), captor.getAllValues().get(1));
    }

    @Test
    public void testCreateImportedBotInPlace_existingLinkedBots_cleared() throws DatabaseException {
        setupFakeImport();
        AiBot linkedBot1 = TestDataHelper.getAiBot(1, "linked1");
        AiBot linkedBot2 = TestDataHelper.getAiBot(2, "linked2");
        List<AiBot> linkedBots = Arrays.asList(linkedBot1, linkedBot2);
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any(), any())).thenReturn(linkedBots);

        this.aiLogic.importBotInPlace(VALIDDEVID, AIID, getBotstructure());
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(this.fakeDatabaseAi, times(linkedBots.size())).unlinkBotFromAi(eq(VALIDDEVID), eq(AIID), captor.capture(), any());
        Assert.assertEquals(linkedBot1.getBotId(), captor.getAllValues().get(0).intValue());
        Assert.assertEquals(linkedBot2.getBotId(), captor.getAllValues().get(1).intValue());
    }

    @Test
    public void testCreateImportedBotInPlace_existingLinkedBots_updated() throws DatabaseException {
        setupFakeImport();
        AiBot linkedBot1 = TestDataHelper.getAiBot(1, "to_unlink");
        AiBot linkedBot2 = TestDataHelper.getAiBot(2, "to_link");

        // Existing bot has linked skill 1 already
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any(), any())).thenReturn(Collections.singletonList(linkedBot1));

        // Bot to import has skill 2 only
        BotStructure botToImport = getBotstructure();
        botToImport.setLinkedSkills(Collections.singletonList(linkedBot2.getBotId()));

        // So the resulting bot should only contain skill 2 (removing skill 1 and adding skill 2)
        this.aiLogic.importBotInPlace(VALIDDEVID, AIID, botToImport);

        // Confirm bot1 has been unlinked
        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
        verify(this.fakeDatabaseAi).unlinkBotFromAi(eq(VALIDDEVID), eq(AIID), captor.capture(), any());
        Assert.assertEquals(linkedBot1.getBotId(), captor.getAllValues().get(0).intValue());

        // Confirm bot2 has been linked
        captor = ArgumentCaptor.forClass(Integer.class);
        verify(this.fakeDatabaseAi).linkBotToAi(eq(VALIDDEVID), eq(AIID), captor.capture(), any());
        Assert.assertEquals(linkedBot2.getBotId(), captor.getAllValues().get(0).intValue());
    }

    private void setupFakeImport() throws DatabaseException {
        ApiAi ai = TestDataHelper.getSampleAI();
        UUID newAiid = UUID.fromString(ai.getAiid());
        when(this.fakeTools.createNewRandomUUID()).thenReturn(newAiid);
        when(this.fakeDatabaseAi.createAI(any(), anyString(), anyString(), any(), anyBoolean(), anyString(),
                any(), anyString(), anyDouble(), anyInt(), anyInt(), any(), anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(UUID.fromString(ai.getAiid()));

        // Loading up the AI after creating it
        when(this.fakeDatabaseAi.getAI(any(UUID.class), any(UUID.class), any(JsonSerializer.class), any(DatabaseTransaction.class))).thenReturn(ai);
        when(this.fakeDatabaseAi.getAIWithStatusForEngineVersion(any(UUID.class), any(UUID.class), anyString(), any(JsonSerializer.class), any(DatabaseTransaction.class))).thenReturn(ai);

        // Called by uploadAndStartTraining
        when(this.fakeDatabaseAi.getAIWithStatusForEngineVersion(any(UUID.class), any(UUID.class), anyString(), any(JsonSerializer.class))).thenReturn(ai);

        when(this.fakeDatabaseAi.updatePassthroughUrl(any(), any(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseAi.updateDefaultChatResponses(any(), any(), any(), any(), any())).thenReturn(true);
        when(this.fakeDatabaseEntitiesIntents.createWebHook(any(), anyString(), anyString(), anyBoolean(), any())).thenReturn(true);
        when(this.fakeAiServices.getTrainingMaterialsCommon(any(), any(), any())).thenReturn("");
    }

    private BotStructure getBotstructure() {
        Map<String, ApiEntity> entities = new HashMap<>();
        entities.put("ent1", new ApiEntity("ent1", VALIDDEVID));
        List<ApiIntent> intents = new ArrayList<>();
        final String intentName = "intent1";
        ApiIntent intent = new ApiIntent(intentName, "in", "out");
        intent.setWebHook(new WebHook(AIID, intentName, "http//endpoint", true));
        intents.add(intent);

        return new BotStructure("ImportedBot", "Desc", intents,
                "Q\nA", entities, 1, false, 1, 0.4f, 1, "EN-en", "UTC",
                Collections.singletonList(TestDataHelper.DEFAULT_CHAT_RESPONSE),
                null, Collections.emptyList(), "", -1, -1, null);
    }

    private List<AiBot> generateBotsWithIds(final List<Integer> idList) {
        List<AiBot> bots = new ArrayList<>();
        for (Integer id : idList) {
            AiBot bot = new AiBot(SAMPLEBOT);
            bot.setBotId(id);
            bots.add(bot);
        }
        return bots;
    }

    private AiBotConfig generateAiBotConfig() {
        Map<String, String> configApiKeys = new HashMap<>();
        configApiKeys.put("key1", "value1");
        configApiKeys.put("key2", "value2");
        return new AiBotConfig(configApiKeys);
    }

    private Pair<UUID, UUID> generateLinkedBotIds() {
        return new Pair<>(UUID.randomUUID(), UUID.randomUUID());
    }

    private AiBotConfigDefinition generateAiBotConfigDefinition() {
        List<AiBotConfigDefinition.ApiKeyDescription> apiKeyDescriptions = new ArrayList<>();
        apiKeyDescriptions.add(new AiBotConfigDefinition.ApiKeyDescription("key1", "desc", "http://blah"));
        apiKeyDescriptions.add(new AiBotConfigDefinition.ApiKeyDescription("key2", "desc", "http://blah"));
        return new AiBotConfigDefinition(apiKeyDescriptions);
    }

    private AiBotConfigWithDefinition generateAiBotConfigWithDefinition() {
        AiBotConfig config = generateAiBotConfig();
        AiBotConfigDefinition definition = generateAiBotConfigDefinition();
        return new AiBotConfigWithDefinition(config, definition);
    }

    private ArrayList<Pair<ApiAi, String>> getAIList() {
        ArrayList<Pair<ApiAi, String>> returnList = new ArrayList<>();
        returnList.add(new Pair<>(TestDataHelper.getSampleAI(), ServiceIdentity.DEFAULT_VERSION));
        return returnList;
    }

    private AiBot generateSkillToLink(final int botId) {
        AiBot botToLink = new AiBot(SAMPLEBOT);
        botToLink.setBotId(botId);
        botToLink.setPublishingType(AiBot.PublishingType.SKILL);
        return botToLink;
    }

    private List<AiBot> generateBots(final int numBots) throws DatabaseException {
        List<AiBot> bots = new ArrayList<>();
        for (int i = 1; i <= numBots; i++) {
            AiBot bot = new AiBot(SAMPLEBOT);
            bot.setBotId(i);
            bots.add(bot);
            when(this.fakeDatabaseMarketplace.getBotDetails(bot.getBotId())).thenReturn(bot);
        }
        return bots;
    }

    private void verifyCannotLinkMoreBots(final List<AiBot> alreadyLinkedBots, final AiBot botToLink,
                                          final int maxLinkedBots) throws DatabaseException {
        when(this.fakeDatabaseMarketplace.getBotDetails(botToLink.getBotId())).thenReturn(botToLink);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.getBotsLinkedToAi(any(), any())).thenReturn(alreadyLinkedBots);
        // Limit the maximum number of bots to 1, so that we're already at the limit
        when(this.fakeConfig.getMaxLinkedBotsPerAi()).thenReturn(maxLinkedBots);
        ApiResult result = this.aiLogic.linkBotToAI(DEVID_UUID, AIID, botToLink.getBotId());
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    private ApiBotStructure setupBotExport(final ApiIntent intent) throws DatabaseException {
        ApiEntity intentEntity = new ApiEntity("entity", VALIDDEVID);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(Collections.singletonList("intent_name"));
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenReturn(intent);
        when(this.fakeDatabaseEntitiesIntents.getEntity(any(), any())).thenReturn(intentEntity);
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn("hello\nhi");

        return (ApiBotStructure) this.aiLogic.exportBotData(VALIDDEVID, AIID);
    }

    private ApiResult callDefaultUpdateAI() {
        return this.aiLogic.updateAI(DEVID_UUID, AIID, "desc", true, 0, 0.0, 0,
                Locale.getDefault(), TimeZone.getDefault().toString(), DEFAULT_CHAT_RESPONSES, -1, -1, null);
    }

    private ApiResult callDefaultCreateAI() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any(JsonSerializer.class), any())).thenReturn(getSampleAI());
        when(this.fakeDatabaseAi.getAI(any(), any(), any(JsonSerializer.class))).thenReturn(getSampleAI());
        return this.aiLogic.createAI(DEVID_UUID, "name", "description", true, 0, 0.0, 1, DEFAULT_CHAT_RESPONSES, null, "", -1, -1, "handover");
    }

    private BotStructure setupImportEntityValueTests(final String entityName,
                                                     final List<String> existingValues,
                                                     final List<String> newValues)
            throws DatabaseException {
        setupFakeImport();
        ApiEntity newEntity = new ApiEntity(entityName, DEVID_UUID, newValues, false);
        BotStructure botStructure = getBotstructure();
        botStructure.setEntities(ImmutableMap.of(entityName, newEntity));

        Entity existingEntitySimple = new Entity(entityName, false);
        ApiEntity existingEntity = new ApiEntity(entityName, DEVID_UUID, existingValues, false);
        when(this.fakeDatabaseEntitiesIntents.getEntities(any())).thenReturn(Collections.singletonList(existingEntitySimple));
        when(this.fakeDatabaseEntitiesIntents.getEntity(any(), any())).thenReturn(existingEntity);

        return botStructure;
    }
}

