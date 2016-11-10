package com.hutoma.api.tests.service;

import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.endpoints.AIServicesStatusEndpoint;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 * Created by pedrotei on 08/11/16.
 */
public class TestServiceAiServices extends ServiceTestBase {

    private static final int HTTP_SERVER_PORT = 9090;
    private HttpServer httpServer;

    @Before
    public void setup() {
        // Create a web server for testing the client methods of AIServices
        URI httpUri = UriBuilder.fromPath("/").scheme("http").host("0.0.0.0").port(HTTP_SERVER_PORT).build();
        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(httpUri);
        try {
            this.httpServer.start();
        } catch (IOException e) {
            System.out.println("HTTP server failed to start");
        }
    }

    @After
    public void teardown() {
        this.httpServer.shutdown();
    }

    protected Class<?> getClassUnderTest() {
        return AIServicesStatusEndpoint.class;
    }

    protected AbstractBinder addAdditionalBindings(AbstractBinder binder) {
        binder.bind(AIServices.class).to(AIServices.class);
        return binder;
    }
}
