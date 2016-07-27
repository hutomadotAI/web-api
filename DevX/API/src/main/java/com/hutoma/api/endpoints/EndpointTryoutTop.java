package com.hutoma.api.endpoints;

import com.google.inject.Injector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by David MG on 25/07/2016.
 */
@Path("/")
public class EndpointTryoutTop extends Endpoint {

    public EndpointTryoutTop() throws Exception {
        super();
    }

    public EndpointTryoutTop(Injector guiceInjector) {
        super(guiceInjector);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String testOut() {
        return "Top Level";
    }
}
