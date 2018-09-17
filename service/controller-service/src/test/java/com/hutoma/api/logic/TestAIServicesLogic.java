package com.hutoma.api.logic;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.QueueAction;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerGeneric;
import com.hutoma.api.controllers.ControllerMap;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.ILogger;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.net.HttpURLConnection;
import java.util.UUID;

import static org.mockito.Mockito.*;


public class TestAIServicesLogic {

    private static final String ENDPOINTID = "fake";
    private static final BackendServerType AI_ENGINE = BackendServerType.EMB;
    private static final java.lang.String ALT_ENDPOINTID = "wrong server";
    private JsonSerializer fakeSerializer;
    private DatabaseAiStatusUpdates fakeDatabase;
    private AiServiceStatusLogger fakeServicesStatusLogger;
    private ILogger fakeLogger;
    private ControllerGeneric fakeController;

    private AIServicesLogic aiServicesLogic;
    private BackendEngineStatus backendStatus;
    private ControllerMap fakeControllerMap;
    private Provider<ControllerGeneric> fakeControllerProvider;

    @Before
    public void setup() throws DatabaseException {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeController = mock(ControllerGeneric.class);
        this.fakeControllerProvider = mock(Provider.class);
        this.fakeControllerMap = new ControllerMap(mock(ControllerAiml.class), this.fakeControllerProvider,
                mock(ILogger.class));

        when(this.fakeControllerProvider.get()).thenReturn(this.fakeController);
        this.aiServicesLogic = new AIServicesLogic(this.fakeSerializer, this.fakeDatabase,
                this.fakeServicesStatusLogger, this.fakeLogger, this.fakeControllerMap);
        when(this.fakeController.getSessionServerIdentifier(eq(TestDataHelper.SESSIONID))).thenReturn(ENDPOINTID);
        when(this.fakeController.isActiveSession(eq(TestDataHelper.SESSIONID))).thenReturn(true);
        when(this.fakeController.getSessionServerIdentifier(eq(TestDataHelper.ALT_SESSIONID))).thenReturn(ALT_ENDPOINTID);
        when(this.fakeController.isActiveSession(eq(TestDataHelper.ALT_SESSIONID))).thenReturn(true);
        this.backendStatus = new BackendEngineStatus(TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, 0.0, 0.0,
                QueueAction.NONE, ENDPOINTID, new DateTime(0));
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.backendStatus);
    }

    @Test
    public void testUpdateAiStatus() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(any())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_db_returns_nothing() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(null);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_dbException() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(any())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_doubleNaN() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(any())).thenThrow(DatabaseException.class);
        status.setTrainingError(Double.NaN);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_hashCode() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, BackendServerType.EMB, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(any())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeController, times(1)).setHashCodeFor(TestDataHelper.AIID, "hash");
    }

    @Test
    public void testUpdateAiStatus_badSession() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                UUID.randomUUID());
        when(this.fakeDatabase.updateAIStatus(any())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_deletedbot() {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        this.backendStatus.setDeleted(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_badStateTransition() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_UNDEFINED, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(any())).thenReturn(false);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_botGetsRequeued() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING_QUEUED, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        verify(this.fakeDatabase, atLeast(1))
                .queueUpdate(any(), any(), eq(true), anyInt(), any());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_reject_wrongServer_ifTraining() {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_UNDEFINED, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.ALT_SESSIONID);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_accept_differentServer() throws DatabaseException {
        this.backendStatus = new BackendEngineStatus(TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, 0.0, 0.0,
                QueueAction.NONE, ENDPOINTID, new DateTime(0));
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.backendStatus);
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING_STOPPED, AI_ENGINE, SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.ALT_SESSIONID);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_reject_wrongServer_ifQueueToTrain() throws DatabaseException {
        this.backendStatus = new BackendEngineStatus(TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING_QUEUED, 0.0, 0.0,
                QueueAction.NONE, ENDPOINTID, new DateTime(0));
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.backendStatus);
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, AI_ENGINE,SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.ALT_SESSIONID);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_ignore_status_trainingstopped() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING_STOPPED, AI_ENGINE,SupportedLanguage.EN, ServiceIdentity.DEFAULT_VERSION,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        verify(this.fakeDatabase, never()).updateAIStatus(any());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }



}
