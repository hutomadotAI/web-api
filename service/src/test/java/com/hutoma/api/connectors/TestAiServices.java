package com.hutoma.api.connectors;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.ThreadPool;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.controllers.ServerMetadata;
import com.hutoma.api.logic.TestIntentLogic;
import com.hutoma.api.memory.MemoryIntentHandler;
import junitparams.JUnitParamsRunner;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.UUID;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 08/11/16.
 */
@RunWith(JUnitParamsRunner.class)
public class TestAiServices {

    private static final UUID DEVID = UUID.fromString("780416b3-d8dd-4283-ace5-65cd5bc987cb");
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final String WNET_ENDPOINT = "http://wnet/endpoint1";
    private static final String RNN_ENDPOINT = "http://rnn/endpoint1";
    private static final DevPlan DEVPLAN = new DevPlan(10, 1000, 5000, 120);
    private static final String AI_ENGINE = "MOCKENGINE";

    private JsonSerializer fakeSerializer;
    private DatabaseAiStatusUpdates fakeDatabase;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private Config fakeConfig;
    private ILogger fakeLogger;
    private Tools fakeTools;
    private JerseyClient fakeClient;
    private AiServiceStatusLogger fakeServicesStatusLogger;
    private ControllerWnet fakeControllerWnet;
    private ControllerRnn fakeControllerRnn;

    private AIQueueServices fakeQueueServices;
    private AIServices aiServices;

    @Before
    public void setup() throws ServerMetadata.NoServerAvailable {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeTools = mock(Tools.class);
        this.fakeClient = mock(JerseyClient.class);
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);
        this.fakeControllerWnet = mock(ControllerWnet.class);
        this.fakeControllerRnn = mock(ControllerRnn.class);
        this.fakeQueueServices = mock(AIQueueServices.class);

        when(this.fakeConfig.getThreadPoolMaxThreads()).thenReturn(32);
        when(this.fakeConfig.getThreadPoolIdleTimeMs()).thenReturn(10000L);
        ThreadPool threadPool = new ThreadPool(this.fakeConfig);

        when(this.fakeControllerWnet.getBackendEndpoint(any(), any())).thenReturn(
                TestDataHelper.getEndpointFor(WNET_ENDPOINT));
        when(this.fakeControllerRnn.getBackendEndpoint(any(), any())).thenReturn(
                TestDataHelper.getEndpointFor(RNN_ENDPOINT));
        this.aiServices = new AIServices(this.fakeDatabase, this.fakeDatabaseEntitiesIntents, this.fakeLogger, this.fakeSerializer,
                this.fakeTools, this.fakeConfig, this.fakeClient, new ThreadSubPool(threadPool),
                this.fakeControllerWnet, this.fakeControllerRnn, this.fakeQueueServices);
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
        this.aiServices.uploadTraining(null, DEVID, AIID, "training materials");
    }

    // Bug:2300
    @Test
    public void testUpload_hack_removeIntentExpressions() {
        Assert.assertEquals(sysIndependent("line1\nline2"),
                this.aiServices.removeIntentExpressions(sysIndependent("line1\nline2\n\nintent expr\n"
                        + MemoryIntentHandler.META_INTENT_TAG + "name")));
        Assert.assertEquals(sysIndependent("line1\nline2"),
                this.aiServices.removeIntentExpressions(sysIndependent("intent expr\n"
                        + MemoryIntentHandler.META_INTENT_TAG + "name\n\nline1\nline2")));
        Assert.assertEquals(sysIndependent(""),
                this.aiServices.removeIntentExpressions(sysIndependent("intent expr\n"
                        + MemoryIntentHandler.META_INTENT_TAG + "name")));
        Assert.assertEquals(sysIndependent("line1\nline2"),
                this.aiServices.removeIntentExpressions(sysIndependent("line1\nline2\n")));
    }

    @Test
    public void testGetTrainingMaterialsCommon() throws Database.DatabaseException {
        final String userTrainingFile = "line1\nline2";
        final ApiIntent intent = TestIntentLogic.getIntent();
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabase.getAiTrainingFile(any())).thenReturn(userTrainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(Collections.singletonList(intent.getIntentName()));
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenReturn(intent);
        String trainingFile = this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);

        final String expectedTrainingFile = String.format("%s\n%s\n%s%s\n", userTrainingFile, intent.getUserSays().get(0),
                MemoryIntentHandler.META_INTENT_TAG, intent.getIntentName());
        Assert.assertEquals(expectedTrainingFile, trainingFile);
    }

    @Test
    public void testGetTrainingMaterialsCommon_noIntents() throws Database.DatabaseException {
        final String userTrainingFile = "line1\nline2";
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabase.getAiTrainingFile(any())).thenReturn(userTrainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(Collections.emptyList());
        String trainingFile = this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
        Assert.assertEquals(userTrainingFile, trainingFile);
    }

    @Test
    public void testGetTrainingMaterialsCommon_aiNotFound() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(null);
        String trainingFile = this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
        Assert.assertNull(trainingFile);
    }

    @Test(expected = Database.DatabaseException.class)
    public void testGetTrainingMaterialsCommon_getAiException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenThrow(Database.DatabaseException.class);
        this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
    }

    @Test(expected = Database.DatabaseException.class)
    public void testGetTrainingMaterialsCommon_getAiTrainingFileException() throws Database.DatabaseException {
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabase.getAiTrainingFile(any())).thenThrow(Database.DatabaseException.class);
        this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
    }

    @Test(expected = Database.DatabaseException.class)
    public void testGetTrainingMaterialsCommon_getIntentsException() throws Database.DatabaseException {
        final String userTrainingFile = "line1\nline2";
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabase.getAiTrainingFile(any())).thenReturn(userTrainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenThrow(Database.DatabaseException.class);
        this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
    }

    @Test(expected = Database.DatabaseException.class)
    public void testGetTrainingMaterialsCommon_getIntentException() throws Database.DatabaseException {
        final String userTrainingFile = "line1\nline2";
        final ApiIntent intent = TestIntentLogic.getIntent();
        when(this.fakeDatabase.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabase.getAiTrainingFile(any())).thenReturn(userTrainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(Collections.singletonList(intent.getIntentName()));
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenThrow(Database.DatabaseException.class);
        this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
    }

    public static String sysIndependent(String data) {
        return data.replace("\n", System.getProperty("line.separator"));
    }

    private void testCommand(CheckedByConsumer<UUID, UUID> logicMethod, String verb)
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

    private void testCommand_serverError(CheckedByConsumer<UUID, UUID> logicMethod, String verb)
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

    private void testCommand_response_noEntity(CheckedByConsumer<UUID, UUID> logicMethod, String verb)
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
