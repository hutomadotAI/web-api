package com.hutoma.api.logic;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeTimerTools;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseAiStatusUpdates;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import junitparams.JUnitParamsRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;


@RunWith(JUnitParamsRunner.class)
public class TestAIServicesLogic {

    private static final BackendServerType AI_ENGINE = BackendServerType.WNET;

    private JsonSerializer fakeSerializer;
    private DatabaseAiStatusUpdates fakeDatabase;
    private Config fakeConfig;
    private Tools fakeTools;
    private AIServices fakeServices;
    private AiServiceStatusLogger fakeServicesStatusLogger;
    private ILogger fakeLogger;
    private ControllerWnet fakeControllerWnet;

    private AIServicesLogic aiServicesLogic;

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeTools = new FakeTimerTools();
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);
        this.fakeServices = mock(AIServices.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeControllerWnet = mock(ControllerWnet.class);
        this.aiServicesLogic = new AIServicesLogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase,
                this.fakeServices, this.fakeServicesStatusLogger, this.fakeLogger, this.fakeTools,
                this.fakeControllerWnet, mock(ControllerRnn.class), mock(ControllerAiml.class));
        when(this.fakeControllerWnet.isActiveSession(eq(TestDataHelper.SESSIONID))).thenReturn(true);
        when(this.fakeControllerWnet.isPrimaryMaster(eq(TestDataHelper.SESSIONID))).thenReturn(true);
    }

    @Test
    public void testUpdateAiStatus() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_db_returns_false() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(false);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_dbException() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_doubleNaN() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenThrow(Database.DatabaseException.class);
        status.setTrainingError(Double.NaN);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_hashCode() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, BackendServerType.WNET,
                0.0, 0.0, "hash",
                TestDataHelper.SESSIONID);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
        verify(this.fakeControllerWnet, times(1)).setHashCodeFor(TestDataHelper.AIID, "hash");
    }

    @Test
    public void testUpdateAiStatus_badSession() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                this.fakeTools.createNewRandomUUID());
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_updateFromWrongServer() throws Database.DatabaseException {

        UUID session = this.fakeTools.createNewRandomUUID();
        when(this.fakeControllerWnet.isActiveSession(eq(session))).thenReturn(true);
        when(this.fakeControllerWnet.isPrimaryMaster(eq(session))).thenReturn(false);

        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID,
                TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE,
                0.0, 0.0, "hash",
                session);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }
}