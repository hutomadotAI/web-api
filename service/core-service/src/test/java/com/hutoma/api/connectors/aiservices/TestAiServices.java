package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.connectors.ServerTrackerInfo;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logic.TestIntentLogic;
import com.hutoma.api.memory.MemoryIntentHandler;
import com.hutoma.api.thread.ThreadPool;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
public class TestAiServices {

    private static final UUID DEVID = UUID.fromString("780416b3-d8dd-4283-ace5-65cd5bc987cb");
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final String WNET_ENDPOINT = "http://wnet/endpoint1";
    private static final String RNN_ENDPOINT = "http://rnn/endpoint1";

    private JsonSerializer fakeSerializer;
    private DatabaseAI fakeDatabaseAi;
    private DatabaseUser fakeDatabaseUser;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private Config fakeConfig;
    private ILogger fakeLogger;
    private Tools fakeTools;
    private JerseyClient fakeClient;
    private ControllerConnector fakeControllerConnector;
    private WnetServicesConnector fakeWnetServicesConnector;
    private RnnServicesConnector fakeRnnServicesConnector;

    private AiServicesQueue fakeQueueServices;
    private AIServices aiServices;

    public static String sysIndependent(String data) {
        return data.replace("\n", System.getProperty("line.separator"));
    }

    @Before
    public void setup() throws NoServerAvailableException {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabaseAi = mock(DatabaseAI.class);
        this.fakeDatabaseUser = mock(DatabaseUser.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeTools = mock(Tools.class);
        this.fakeClient = mock(JerseyClient.class);
        this.fakeQueueServices = mock(AiServicesQueue.class);
        this.fakeControllerConnector = mock(ControllerConnector.class);
        this.fakeWnetServicesConnector = mock(WnetServicesConnector.class);
        this.fakeRnnServicesConnector = mock(RnnServicesConnector.class);

        when(this.fakeConfig.getThreadPoolMaxThreads()).thenReturn(32);
        when(this.fakeConfig.getThreadPoolIdleTimeMs()).thenReturn(10000L);
        ThreadPool threadPool = new ThreadPool(this.fakeConfig);

        when(this.fakeControllerConnector.getBackendEndpoint(AIID, RequestFor.Training, BackendServerType.WNET))
                .thenReturn(TestDataHelper.getEndpointFor(WNET_ENDPOINT));
        when(this.fakeControllerConnector.getBackendEndpoint(AIID, RequestFor.Training, BackendServerType.RNN))
                .thenReturn(TestDataHelper.getEndpointFor(RNN_ENDPOINT));
        when(this.fakeControllerConnector.getBackendEndpoint(null, RequestFor.Training, BackendServerType.WNET))
                .thenReturn(TestDataHelper.getEndpointFor(WNET_ENDPOINT));
        when(this.fakeControllerConnector.getBackendEndpoint(null, RequestFor.Training, BackendServerType.RNN))
                .thenReturn(TestDataHelper.getEndpointFor(RNN_ENDPOINT));

        this.aiServices = new AIServices(this.fakeDatabaseAi, this.fakeDatabaseEntitiesIntents, this.fakeLogger, this.fakeSerializer,
                this.fakeTools, this.fakeClient, new TrackedThreadSubPool(threadPool), this.fakeQueueServices,
                this.fakeWnetServicesConnector, this.fakeRnnServicesConnector);
    }

    @Test
    public void testDeleteDev() throws AIServices.AiServicesException, DatabaseException {
        testCommand((a, b) -> this.aiServices.deleteDev(DEVID), HttpMethod.DELETE);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testDeleteDev_serverError() throws AIServices.AiServicesException, DatabaseException, NoServerAvailableException {
        fakeBackendServicesRegistered();
        testCommand_serverError((a, b) -> this.aiServices.deleteDev(DEVID), HttpMethod.DELETE);
    }

    @Test(expected = AIServices.AiServicesException.class)
    public void testDeleteDev_response_noEntity() throws AIServices.AiServicesException, DatabaseException {
        fakeBackendServicesRegistered();
        testCommand_response_noEntity((a, b) -> this.aiServices.deleteDev(DEVID), HttpMethod.DELETE);
    }

    @Test
    public void testUploadTraining() throws AIServices.AiServicesException, NoServerAvailableException {
        JerseyInvocation.Builder builder = getFakeBuilder();
        IServerEndpoint endpoint = getFakeServerEndpoint();
        when(this.fakeWnetServicesConnector.getBackendEndpoint(any(), any())).thenReturn(endpoint);
        when(this.fakeRnnServicesConnector.getBackendEndpoint(any(), any())).thenReturn(endpoint);
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
    public void testGetTrainingMaterialsCommon() throws DatabaseException {
        final String userTrainingFile = "line1\nline2";
        final ApiIntent intent = TestIntentLogic.getIntent();
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(userTrainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(Collections.singletonList(intent.getIntentName()));
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenReturn(intent);
        String trainingFile = this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);

        final String expectedTrainingFile = String.format("%s\n%s\n%s%s\n", userTrainingFile, intent.getUserSays().get(0),
                MemoryIntentHandler.META_INTENT_TAG, intent.getIntentName());
        Assert.assertEquals(expectedTrainingFile, trainingFile);
    }

    @Test
    public void testGetTrainingMaterialsCommon_noIntents() throws DatabaseException {
        final String userTrainingFile = "line1\nline2";
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(userTrainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(Collections.emptyList());
        String trainingFile = this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
        Assert.assertEquals(userTrainingFile, trainingFile);
    }

    @Test
    public void testGetTrainingMaterialsCommon_aiNotFound() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(null);
        String trainingFile = this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
        Assert.assertNull(trainingFile);
    }

    @Test(expected = DatabaseException.class)
    public void testGetTrainingMaterialsCommon_getAiException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenThrow(DatabaseException.class);
        this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
    }

    @Test(expected = DatabaseException.class)
    public void testGetTrainingMaterialsCommon_getAiTrainingFileException() throws DatabaseException {
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenThrow(DatabaseException.class);
        this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
    }

    @Test(expected = DatabaseException.class)
    public void testGetTrainingMaterialsCommon_getIntentsException() throws DatabaseException {
        final String userTrainingFile = "line1\nline2";
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(userTrainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenThrow(DatabaseException.class);
        this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
    }

    @Test(expected = DatabaseException.class)
    public void testGetTrainingMaterialsCommon_getIntentException() throws DatabaseException {
        final String userTrainingFile = "line1\nline2";
        final ApiIntent intent = TestIntentLogic.getIntent();
        when(this.fakeDatabaseAi.getAI(any(), any(), any())).thenReturn(TestDataHelper.getSampleAI());
        when(this.fakeDatabaseAi.getAiTrainingFile(any())).thenReturn(userTrainingFile);
        when(this.fakeDatabaseEntitiesIntents.getIntents(any(), any())).thenReturn(Collections.singletonList(intent.getIntentName()));
        when(this.fakeDatabaseEntitiesIntents.getIntent(any(), any())).thenThrow(DatabaseException.class);
        this.aiServices.getTrainingMaterialsCommon(DEVID, AIID, this.fakeSerializer);
    }

    private void testCommand(CheckedByConsumer<UUID, UUID> logicMethod, String verb)
            throws AIServices.AiServicesException, DatabaseException {
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

    private void fakeBackendServicesRegistered() {
        Map<String, ServerTrackerInfo> map = new HashMap<>();
        map.put("key", new ServerTrackerInfo("url", "ident", 1, 1, true, true));
        when(this.fakeWnetServicesConnector.getVerifiedEndpointMap()).thenReturn(map);
        when(this.fakeRnnServicesConnector.getVerifiedEndpointMap()).thenReturn(map);
    }

    private void testCommand_serverError(CheckedByConsumer<UUID, UUID> logicMethod, String verb)
            throws AIServices.AiServicesException, DatabaseException {
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
            throws AIServices.AiServicesException, DatabaseException {
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

    static IServerEndpoint getFakeServerEndpoint(final String url) {
        return new IServerEndpoint() {
            @Override
            public String getServerUrl() {
                return url;
            }

            @Override
            public String getServerIdentifier() {
                return "ident";
            }
        };
    }

    static IServerEndpoint getFakeServerEndpoint() {
        return getFakeServerEndpoint("http://url.com");
    }
}
