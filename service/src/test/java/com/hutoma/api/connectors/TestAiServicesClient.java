package com.hutoma.api.connectors;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.TestDataHelper;
import com.hutoma.api.common.ThreadPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.common.TrackedThreadSubPool;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.controllers.ServerMetadata;
import com.hutoma.api.memory.MemoryIntentHandler;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.hutoma.api.connectors.TestAiServices.sysIndependent;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 10/11/16.
 */
public class TestAiServicesClient {

    private static final UUID DEVID = UUID.fromString("1a5c55e7-6492-4d08-8dfd-d167ac9f3330");
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final String COMMAND_PARAM = "command";
    private static final String LOCAL_WEB_SERVER = "http://127.0.0.1:9090";
    private static final String LOCAL_ENDPOINT_PATH = "training";
    private static final String LOCAL_WEB_ENDPOINT = LOCAL_WEB_SERVER + "/" + LOCAL_ENDPOINT_PATH;
    private static final String TRAINING_MATERIALS_NO_INTENT = sysIndependent(
            "question1\nanswer1\nquestion2\nanswer2");
    private static final String TRAINING_MATERIALS = TRAINING_MATERIALS_NO_INTENT +
            sysIndependent("\n\nintent expression\n" + MemoryIntentHandler.META_INTENT_TAG + "myintent");
    private static final DevPlan DEVPLAN = new DevPlan(10, 1000, 5000, 120);

    private static HttpServer httpServer;
    private JsonSerializer fakeSerializer;
    private DatabaseAiStatusUpdates fakeDatabase;
    private DatabaseEntitiesIntents fakeDatabaseEntitiesIntents;
    private Config fakeConfig;
    private ILogger fakeLogger;
    private Tools fakeTools;
    private AIServices aiServices;
    private AiServiceStatusLogger fakeServicesStatusLogger;
    private ControllerWnet fakeControllerWnet;
    private ControllerRnn fakeControllerRnn;
    private ThreadPool threadPool;
    private AIQueueServices fakeQueueServices;

    @BeforeClass
    public static void initializeClass() {
        final ResourceConfig rc = new ResourceConfig(TestServer.class);
        rc.register(MultiPartFeature.class);
        httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(LOCAL_WEB_SERVER), rc);
    }

    @AfterClass
    public static void cleanupClass() {
        httpServer.shutdownNow();
    }

    @Before
    public void setup() throws ServerMetadata.NoServerAvailable {
        this.fakeSerializer = mock(JsonSerializer.class);
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(DatabaseAiStatusUpdates.class);
        this.fakeDatabaseEntitiesIntents = mock(DatabaseEntitiesIntents.class);
        this.fakeLogger = mock(ILogger.class);
        this.fakeTools = mock(Tools.class);
        this.fakeServicesStatusLogger = mock(AiServiceStatusLogger.class);
        this.fakeQueueServices = mock(AIQueueServices.class);

        this.fakeControllerWnet = mock(ControllerWnet.class);
        this.fakeControllerRnn = mock(ControllerRnn.class);

        when(this.fakeConfig.getThreadPoolMaxThreads()).thenReturn(32);
        when(this.fakeConfig.getThreadPoolIdleTimeMs()).thenReturn(10000L);
        this.threadPool = new ThreadPool(this.fakeConfig);

        when(this.fakeControllerWnet.getBackendEndpoint(any(), any())).thenReturn(
                TestDataHelper.getEndpointFor(LOCAL_WEB_ENDPOINT));
        when(this.fakeControllerRnn.getBackendEndpoint(any(), any())).thenReturn(
                TestDataHelper.getEndpointFor(LOCAL_WEB_ENDPOINT));
        this.aiServices = new AIServices(this.fakeDatabase, this.fakeDatabaseEntitiesIntents, this.fakeLogger, this.fakeSerializer,
                this.fakeTools, this.fakeConfig, JerseyClientBuilder.createClient(), new TrackedThreadSubPool(this.threadPool),
                this.fakeControllerWnet, this.fakeControllerRnn, this.fakeQueueServices);
    }

    @Test
    public void testStartTraining() throws AIServices.AiServicesException, Database.DatabaseException {
        when(this.fakeDatabase.getDevPlan(DEVID)).thenReturn(DEVPLAN);
        this.aiServices.startTraining(null, DEVID, AIID);
    }

    @Test
    public void testStopTraining() throws AIServices.AiServicesException {
        this.aiServices.stopTraining(null, DEVID, AIID);
    }

    @Test
    public void testDeleteAi() throws AIServices.AiServicesException {
        this.aiServices.deleteAI(null, DEVID, AIID);
    }

    @Test
    public void testDeleteDev() throws AIServices.AiServicesException {
        this.aiServices.deleteDev(DEVID);
    }

    @Test
    public void testUploadTraining() throws AIServices.AiServicesException {
        // Need to have a real serializer here to transform the ai info
        AIServices thisAiServices = new AIServices(this.fakeDatabase, this.fakeDatabaseEntitiesIntents, this.fakeLogger, new JsonSerializer(),
                this.fakeTools, this.fakeConfig, JerseyClientBuilder.createClient(), new TrackedThreadSubPool(this.threadPool),
                this.fakeControllerWnet, this.fakeControllerRnn, this.fakeQueueServices);
        thisAiServices.uploadTraining(null, DEVID, AIID, TRAINING_MATERIALS);
    }

    @Path("/training")
    public static class TestServer {
        private final JsonSerializer serializer = new JsonSerializer();

        @POST
        @Path("{devId}/{aiid}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response sendOkForDevIdAiidCommandPost(
                @PathParam("devId") String devId,
                @PathParam("aiid") String aiid,
                @QueryParam(COMMAND_PARAM) String command) {
            try {
                checkParameterValue(DEVID.toString(), devId);
                checkParameterValue(AIID.toString(), aiid);
                checkParameterValue(Arrays.asList("start", "stop", "wake"), command);
            } catch (Exception ex) {
                return ApiError.getBadRequest(ex.getMessage()).getResponse(this.serializer).build();
            }
            return new ApiResult().setSuccessStatus().getResponse(this.serializer).build();
        }

        @DELETE
        @Path("{devId}/{aiid}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response sendOkForDevIdAiidDelete(
                @PathParam("devId") String devId,
                @PathParam("aiid") String aiid) {
            try {
                checkParameterValue(DEVID.toString(), devId);
                checkParameterValue(AIID.toString(), aiid);
            } catch (Exception ex) {
                return ApiError.getBadRequest(ex.getMessage()).getResponse(this.serializer).build();
            }
            return new ApiResult().setSuccessStatus().getResponse(this.serializer).build();
        }

        @DELETE
        @Path("{devId}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response sendOkForDevIdDelete(
                @PathParam("devId") String devId) {
            try {
                checkParameterValue(DEVID.toString(), devId);
            } catch (Exception ex) {
                return ApiError.getBadRequest(ex.getMessage()).getResponse(this.serializer).build();
            }
            return new ApiResult().setSuccessStatus().getResponse(this.serializer).build();
        }

        @PUT
        @Path("{devId}/{aiid}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response sendOkForDevIdAiidPut(
                @PathParam("devId") String devId,
                @PathParam("aiid") String aiid) {
            try {
                checkParameterValue(DEVID.toString(), devId);
                checkParameterValue(AIID.toString(), aiid);
            } catch (Exception ex) {
                return ApiError.getBadRequest(ex.getMessage()).getResponse(this.serializer).build();
            }
            return new ApiResult().setSuccessStatus().getResponse(this.serializer).build();
        }

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public Response sendOkForUploadTraining(
                @FormDataParam("info") FormDataBodyPart infoPart,
                @FormDataParam("filename") String trainingMaterials) {
            try {
                AIServices.AiInfo info = (AIServices.AiInfo) this.serializer.deserialize(
                        infoPart.getValueAs(String.class), AIServices.AiInfo.class);
                checkParameterValue(DEVID.toString(), info.getDevId());
                checkParameterValue(AIID.toString(), info.getAiid());
                // HACK! HACK! Remove when Bug:2300 is fixed
                if (!trainingMaterials.equals(TRAINING_MATERIALS_NO_INTENT)) {
                    checkParameterValue(TRAINING_MATERIALS, trainingMaterials);
                }
            } catch (Exception ex) {
                return ApiError.getBadRequest(ex.getMessage()).getResponse(this.serializer).build();
            }
            return new ApiResult().setSuccessStatus().getResponse(this.serializer).build();
        }

        private void checkParameterValue(final String expected, final String actual) throws Exception {
            if (expected == null && actual == null) {
                return;
            }
            if (expected != null && !expected.equals(actual)) {
                throw new Exception("Parameter mismatch");
            }
        }

        private void checkParameterValue(final List<String> expected, final String actual) throws Exception {
            if (!expected.contains(actual)) {
                throw new Exception("Parameter mismatch");
            }
        }
    }
}
