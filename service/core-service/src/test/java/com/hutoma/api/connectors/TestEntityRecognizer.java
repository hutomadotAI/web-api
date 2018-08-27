package com.hutoma.api.connectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.aiservices.AIServices;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.sub.RecognizedEntity;
import com.hutoma.api.logging.ILogger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by pedrotei on 10/11/16.
 */
public class TestEntityRecognizer {

    private static final String LOCAL_WEB_SERVER_ADDR = "http://0.0.0.0";
    private static final String ENTITY_VALUE = "value1";
    private static final String ENTITY_CATEGORY = "category1";
    private static final int ENTITY_START = 1;
    private static final int ENTITY_END = 2;
    private static final String ER_RESPONSE = String.format(
            "[{\"value\":\"%s\", \"category\":\"%s\",\"start\":%d,\"end\":%d}]",
            ENTITY_VALUE, ENTITY_CATEGORY, ENTITY_START, ENTITY_END);

    private static HttpServer httpServer;
    private static String localServer;
    private EntityRecognizerService erService;
    private Config fakeConfig;

    @BeforeClass
    public static void initializeClass() throws IOException {
        // Get an available listening port
        ServerSocket s = new ServerSocket(0);
        int listeningPort = s.getLocalPort();
        localServer = String.format("%s:%d", LOCAL_WEB_SERVER_ADDR, listeningPort);
        s.close();
        final ResourceConfig rc = new ResourceConfig(TestServer.class);
        rc.register(MultiPartFeature.class);
        httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(localServer), rc);
    }

    @AfterClass
    public static void cleanupClass() {
        httpServer.shutdownNow();
    }

    @Before
    public void setup() {
        this.fakeConfig = mock(Config.class);
        when(this.fakeConfig.getEntityRecognizerUrl()).thenReturn(localServer);
        this.erService = new EntityRecognizerService(mock(ILogger.class), new JsonSerializer(),
                this.fakeConfig, JerseyClientBuilder.createClient());
    }

    @Test
    public void testCallEntityRecognizer() throws AIServices.AiServicesException, DatabaseException {
        List<RecognizedEntity> entities = this.erService.getEntities("anything not null");
        Assert.assertEquals(1, entities.size());
        Assert.assertEquals(ENTITY_CATEGORY, entities.get(0).getCategory());
        Assert.assertEquals(ENTITY_VALUE, entities.get(0).getValue());
        Assert.assertEquals(ENTITY_START, entities.get(0).getStart());
        Assert.assertEquals(ENTITY_END, entities.get(0).getEnd());
    }

    @Test
    public void testCallEntityRecognizer_serverError() throws AIServices.AiServicesException, DatabaseException {
        List<RecognizedEntity> entities = this.erService.getEntities(null);
        Assert.assertEquals(0, entities.size());
    }

    @Path("/")
    public static class TestServer {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response sendResponse(
                @QueryParam("q") String question) {
            if (question == null || question.isEmpty()) {
                return Response.serverError().build();
            }
            return Response.ok().entity(ER_RESPONSE).build();
        }
    }
}
