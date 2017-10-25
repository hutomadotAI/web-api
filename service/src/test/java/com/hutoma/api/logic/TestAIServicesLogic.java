package com.hutoma.api.logic;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.QueueAction;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.net.HttpURLConnection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;


public class TestAIServicesLogic {

    private static final String ENDPOINTID = "fake";
    private static final BackendServerType AI_ENGINE = BackendServerType.WNET;
    private static final java.lang.String ALT_ENDPOINTID = "wrong server";
    private JsonSerializer fakeSerializer;
    private DatabaseAiStatusUpdates fakeDatabase;
    private DatabaseUser fakeDatabaseUser;
    private Config fakeConfig;
    private Tools fakeTools;
    private AIServices fakeServices;
    private AiServiceStatusLogger fakeServicesStatusLogger;
    private ILogger fakeLogger;
    private ControllerWnet fakeControllerWnet;

    private AIServicesLogic aiServicesLogic;
    private BackendEngineStatus backendStatus;

    @Before
    public void setup() throws DatabaseException {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeDatabaseUser = mock(DatabaseUser.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);
        this.fakeServices = mock(AIServices.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeControllerWnet = mock(ControllerWnet.class);
        this.aiServicesLogic = new AIServicesLogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase,
                this.fakeServices, this.fakeServicesStatusLogger, this.fakeLogger, this.fakeTools,
                this.fakeControllerWnet, mock(ControllerRnn.class), mock(ControllerAiml.class));
        when(this.fakeControllerWnet.getSessionServerIdentifier(eq(TestDataHelper.SESSIONID))).thenReturn(ENDPOINTID);
        when(this.fakeControllerWnet.isActiveSession(eq(TestDataHelper.SESSIONID))).thenReturn(true);
        when(this.fakeControllerWnet.getSessionServerIdentifier(eq(TestDataHelper.ALT_SESSIONID))).thenReturn(ALT_ENDPOINTID);
        when(this.fakeControllerWnet.isActiveSession(eq(TestDataHelper.ALT_SESSIONID))).thenReturn(true);
        this.backendStatus = new BackendEngineStatus(TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, 0.0, 0.0,
                QueueAction.NONE, ENDPOINTID, new DateTime(0));
        when(this.fakeDatabase.getAiQueueStatus(any(), any())).thenReturn(this.backendStatus);
    }

    @Test
    public void testUpdateAiStatus() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_db_returns_nothing() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.getAiQueueStatus(anyObject(), any())).thenReturn(null);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_dbException() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject())).thenThrow(DatabaseException.class);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_doubleNaN() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject())).thenThrow(DatabaseException.class);
        status.setTrainingError(Double.NaN);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_hashCode() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, BackendServerType.WNET,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeControllerWnet, times(1)).setHashCodeFor(TestDataHelper.AIID, "hash");
    }

    @Test
    public void testUpdateAiStatus_badSession() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                this.fakeTools.createNewRandomUUID());
        when(this.fakeDatabase.updateAIStatus(anyObject())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_deletedbot() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        this.backendStatus.setDeleted(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_badStateTransition() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_UNDEFINED, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject())).thenReturn(false);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_botGetsRequeued() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING_QUEUED, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        verify(this.fakeDatabase, atLeast(1))
                .queueUpdate(any(), any(), Matchers.eq(true), anyInt(), any());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_reject_wrongServer_ifTraining() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_UNDEFINED, AI_ENGINE,
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
                TrainingStatus.AI_TRAINING_STOPPED, AI_ENGINE,
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
                TrainingStatus.AI_TRAINING, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.ALT_SESSIONID);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_CONFLICT, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_ignore_status_trainingstopped() throws DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_TRAINING_STOPPED, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        verify(this.fakeDatabase, never()).updateAIStatus(any());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }



}