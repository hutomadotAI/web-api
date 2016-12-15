package com.hutoma.api.connectors;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.FakeJsonSerializer;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.DevPlan;

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
import java.util.Collections;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by pedrotei on 10/11/16.
 */
public class TestAiServicesClient {

    private static final String DEVID = "devid";
    private static final UUID AIID = UUID.fromString("41c6e949-4733-42d8-bfcf-95192131137e");
    private static final String COMMAND_PARAM = "command";
    private static final String LOCAL_WEB_SERVER = "http://127.0.0.1:9090";
    private static final String LOCAL_ENDPOINT_PATH = "training";
    private static final String LOCAL_WEB_ENDPOINT = LOCAL_WEB_SERVER + "/" + LOCAL_ENDPOINT_PATH;
    private static final String TRAINING_MATERIALS = "question1\nanswer1\nquestion2\nanswer2\n\nintent expression\n@meta.intent.myintent";
    private static final DevPlan DEVPLAN = new DevPlan(10, 1000, 5000, 120);

    private static HttpServer httpServer;
    private FakeJsonSerializer fakeSerializer;
    private Database fakeDatabase;
    private Config fakeConfig;
    private Logger fakeLogger;
    private Tools fakeTools;
    private AIServices aiServices;

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
    public void setup() {
        this.fakeSerializer = new FakeJsonSerializer();
        this.fakeConfig = mock(Config.class);
        this.fakeDatabase = mock(Database.class);
        this.fakeLogger = mock(Logger.class);
        this.fakeTools = mock(Tools.class);

        this.aiServices = new AIServices(this.fakeDatabase, this.fakeLogger, this.fakeSerializer,
                this.fakeTools, this.fakeConfig, JerseyClientBuilder.createClient());
        when(this.fakeConfig.getRnnTrainingEndpoint()).thenReturn(LOCAL_WEB_ENDPOINT);
        when(this.fakeConfig.getWnetTrainingEndpoint()).thenReturn(LOCAL_WEB_ENDPOINT);
    }

    @Test
    public void testStartTraining() throws AIServices.AiServicesException, Database.DatabaseException {
        when(this.fakeDatabase.getDevPlan(DEVID)).thenReturn(DEVPLAN);
        this.aiServices.startTraining(DEVID, AIID);
    }

    @Test
    public void testStopTraining() throws AIServices.AiServicesException {
        this.aiServices.stopTraining(DEVID, AIID);
    }

    @Test
    public void testUpdateTraining() throws AIServices.AiServicesException {
        this.aiServices.updateTraining(DEVID, AIID);
    }

    @Test
    public void testDeleteAi() throws AIServices.AiServicesException {
        this.aiServices.deleteAI(DEVID, AIID);
    }

    @Test
    public void testDeleteDev() throws AIServices.AiServicesException {
        this.aiServices.deleteDev(DEVID);
    }

    @Test
    public void testUploadTraining() throws AIServices.AiServicesException {
        // Need to have a real serializer here to transform the ai info
        AIServices thisAiServices = new AIServices(this.fakeDatabase, this.fakeLogger, new JsonSerializer(),
                this.fakeTools, this.fakeConfig, JerseyClientBuilder.createClient());
        thisAiServices.uploadTraining(DEVID, AIID, TRAINING_MATERIALS);
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
                checkParameterValue(DEVID, devId);
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
                checkParameterValue(DEVID, devId);
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
                checkParameterValue(DEVID, devId);
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
                checkParameterValue(DEVID, devId);
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
                checkParameterValue(DEVID, info.getDevId());
                checkParameterValue(AIID.toString(), info.getAiid());
                checkParameterValue(TRAINING_MATERIALS, trainingMaterials);
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
