package com.hutoma.api.endpoints;

import com.google.inject.Injector;
import com.hutoma.api.logic.LogicTest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by David MG on 25/07/2016.
 */
@Path("/admin")
public class EndpointTryout extends Endpoint {

    public EndpointTryout() throws Exception {
        super();
    }

    public EndpointTryout(Injector guiceInjector) {
        super(guiceInjector);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String testOut() {
        return guiceInjector.getInstance(LogicTest.class).testOutput("no user");
    }
}
