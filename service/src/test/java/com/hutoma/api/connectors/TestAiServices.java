package com.hutoma.api.connectors;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.containers.sub.TrainingStatus;
import junitparams.JUnitParamsRunner;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.net.HttpURLConnection;
import java.util.UUID;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 08/11/16.
 */
@RunWith(JUnitParamsRunner.class)
public class TestAiServices {

    private static final String DEVID = "devid";
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final String WNET_ENDPOINT = "http://wnet/endpoint1";
    private static final String RNN_ENDPOINT = "http://rnn/endpoint1";
    private static final DevPlan DEVPLAN = new DevPlan(10, 1000, 5000, 120);
    private static final String AI_ENGINE = "MOCKENGINE";

    private JsonSerializer fakeSerializer;
    private SecurityContext fakeContext;
    private Database fakeDatabase;
    private Config fakeConfig;
    private ILogger fakeLogger;
    private Tools fakeTools;
    private JerseyClient fakeClient;
    private AiServiceStatusLogger fakeServicesStatusLogger;

    private AIServices aiServices;

    @Before
    public void setup() {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeContext = mock(SecurityContext.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeTools = mock(Tools.class);
        this.fakeClient = mock(JerseyClient.class);
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);

        when(this.fakeConfig.getWnetTrainingEndpoint()).thenReturn(WNET_ENDPOINT);
        when(this.fakeConfig.getRnnTrainingEndpoint()).thenReturn(RNN_ENDPOINT);
        this.aiServices = new AIServices(this.fakeDatabase, this.fakeLogger, this.fakeSerializer,
                this.fakeTools, this.fakeConfig, this.fakeClient, this.fakeServicesStatusLogger);
    }

    @Test
    public void testStartTraining() throws AIServices.AiServicesException, Database.DatabaseException {
        when(this.fakeDatabase.getDevPlan(DEVID)).thenReturn(DEVPLAN);
        testCommand((a, b) -> this.aiServices.startTraining(DEVID, AIID), HttpMethod.POST);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testStartTraining_serverError() throws AIServices.AiServicesException, Database.DatabaseException {
        when(this.fakeDatabase.getDevPlan(DEVID)).thenReturn(DEVPLAN);
        testCommand_serverError((a, b) -> this.aiServices.startTraining(DEVID, AIID), HttpMethod.POST);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testStartTraining_response_noEntity() throws AIServices.AiServicesException, Database.DatabaseException {
        when(this.fakeDatabase.getDevPlan(DEVID)).thenReturn(DEVPLAN);
        testCommand_response_noEntity((a, b) -> this.aiServices.startTraining(DEVID, AIID), HttpMethod.POST);
    }

    @Test
    public void testStopTraining() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand((a, b) -> this.aiServices.stopTraining(DEVID, AIID), HttpMethod.POST);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testStopTraining_serverError() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand_serverError((a, b) -> this.aiServices.stopTraining(DEVID, AIID), HttpMethod.POST);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testStopTraining_response_noEntity() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand_response_noEntity((a, b) -> this.aiServices.stopTraining(DEVID, AIID), HttpMethod.POST);
    }

    @Test
    public void testDeleteAI() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand((a, b) -> this.aiServices.deleteAI(DEVID, AIID), HttpMethod.DELETE);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testDeleteAI_serverError() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand_serverError((a, b) -> this.aiServices.deleteAI(DEVID, AIID), HttpMethod.DELETE);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testDeleteAI_response_noEntity() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand_response_noEntity((a, b) -> this.aiServices.deleteAI(DEVID, AIID), HttpMethod.DELETE);
    }

    @Test
    public void testDeleteDev() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand((a, b) -> this.aiServices.deleteDev(DEVID), HttpMethod.DELETE);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testDeleteDev_serverError() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand_serverError((a, b) -> this.aiServices.deleteDev(DEVID), HttpMethod.DELETE);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testDeleteDev_response_noEntity() throws AIServices.AiServicesException, Database.DatabaseException {
        testCommand_response_noEntity((a, b) -> this.aiServices.deleteDev(DEVID), HttpMethod.DELETE);
    }

    @Test
    public void testUploadTraining() throws AIServices.AiServicesException {
        JerseyInvocation.Builder builder = getFakeBuilder();
        when(builder.post(any())).thenReturn(Response.ok(new ApiResult().setSuccessStatus()).build());
        this.aiServices.uploadTraining(DEVID, AIID, "training materials");
    }

    @Test
    public void testUpdateAiStatus() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(true);
        ApiResult result = this.aiServices.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_db_returns_false() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenReturn(false);
        ApiResult result = this.aiServices.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_NOT_FOUND, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_dbException() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenThrow(Database.DatabaseException.class);
        ApiResult result = this.aiServices.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, result.getStatus().getCode());
    }

    @Test
    public void testUpdateAiStatus_doubleNaN() throws Database.DatabaseException {
        AiStatus status = new AiStatus(TestDataHelper.DEVID, TestDataHelper.AIID, TrainingStatus.AI_READY_TO_TRAIN, AI_ENGINE, 0.0, 0.0);
        when(this.fakeDatabase.updateAIStatus(anyObject(), any())).thenThrow(Database.DatabaseException.class);
        status.setTrainingError(Double.NaN);
        ApiResult result = this.aiServices.updateAIStatus(status);
        Assert.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, result.getStatus().getCode());
    }

    private void testCommand(CheckedByConsumer<String, UUID> logicMethod, String verb)
            throws AIServices.AiServicesException, Database.DatabaseException {
        JerseyInvocation.Builder builder = getFakeBuilder();
        switch (verb) {
            case HttpMethod.POST:
                when(builder.post(any())).thenReturn(Response.ok(new ApiResult().setSuccessStatus()).build());
                break;
            case HttpMethod.PUT:
                when(builder.put(any())).thenReturn(Response.ok(new ApiResult().setSuccessStatus()).build());
                break;
            case HttpMethod.DELETE:
                when(builder.delete()).thenReturn(Response.ok(new ApiResult().setSuccessStatus()).build());
                break;
            default:
                throw new IllegalArgumentException("Unsupported verb " + verb);
        }
        logicMethod.apply(DEVID, AIID);
    }

    private void testCommand_serverError(CheckedByConsumer<String, UUID> logicMethod, String verb)
            throws AIServices.AiServicesException, Database.DatabaseException {
        JerseyInvocation.Builder builder = getFakeBuilder();
        switch (verb) {
            case HttpMethod.POST:
                when(builder.post(any())).thenReturn(Response.serverError().entity(ApiError.getInternalServerError()).build());
                break;
            case HttpMethod.DELETE:
                when(builder.delete()).thenReturn(Response.serverError().entity(ApiError.getInternalServerError()).build());
                break;
            case HttpMethod.PUT:
                when(builder.put(any())).thenReturn(Response.serverError().entity(ApiError.getInternalServerError()).build());
                break;
            default:
                throw new IllegalArgumentException("Unsupported verb " + verb);
        }
        logicMethod.apply(DEVID, AIID);
    }

    private void testCommand_response_noEntity(CheckedByConsumer<String, UUID> logicMethod, String verb)
            throws AIServices.AiServicesException, Database.DatabaseException {
        JerseyInvocation.Builder builder = getFakeBuilder();
        switch (verb) {
            case HttpMethod.POST:
                when(builder.post(any())).thenReturn(Response.serverError().entity(null).build());
                break;
            case HttpMethod.DELETE:
                when(builder.delete()).thenReturn(Response.serverError().entity(null).build());
                break;
            case HttpMethod.PUT:
                when(builder.put(any())).thenReturn(Response.serverError().entity(null).build());
                break;
            default:
                throw new IllegalArgumentException("Unsupported verb " + verb);
        }
        logicMethod.apply(DEVID, AIID);

    }

    private JerseyInvocation.Builder getFakeBuilder() {
        JerseyWebTarget jerseyWebTarget = Mockito.mock(JerseyWebTarget.class);
        JerseyInvocation.Builder builder = Mockito.mock(JerseyInvocation.Builder.class);
        when(this.fakeClient.target(any(String.class))).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.path(anyString())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.queryParam(anyString(), anyString())).thenReturn(jerseyWebTarget);
        when(jerseyWebTarget.request()).thenReturn(builder);
        return builder;
    }

    @FunctionalInterface
    private interface CheckedByConsumer<T, U> {
        void apply(T t, U u) throws AIServices.AiServicesException;
    }
}
