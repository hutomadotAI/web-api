package com.hutoma.api.logic;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.TrainingStatus;
import junitparams.JUnitParamsRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(JUnitParamsRunner.class)
public class TestAIServicesLogic {

    private static final String AI_ENGINE = "MOCKENGINE";

    private JsonSerializer fakeSerializer;
    private Database fakeDatabase;
    private Config fakeConfig;
    private Tools fakeTools;
    private AIServices fakeServices;
    private AiServiceStatusLogger fakeServicesStatusLogger;
    private ILogger fakeLogger;

    private AIServicesLogic aiServicesLogic;

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeTools = mock(Tools.class);
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);
        this.fakeServices = mock(AIServices.class);
        this.fakeLogger = mock(ILogger.class);
        this.aiServicesLogic = new AIServicesLogic(this.fakeConfig, this.fakeSerializer, this.fakeDatabase,
                this.fakeServices, this.fakeServicesStatusLogger, this.fakeLogger, this.fakeTools, null, null, null);
    }

    @Test
    public void testUpdateAiStatus() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(true);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_db_returns_false() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(false);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_dbException() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_doubleNaN() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenThrow(Database.DatabaseException.class);
        status.setTrainingError(Double.NaN);
        ApiResult result = this.aiServicesLogic.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

}