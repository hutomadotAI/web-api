package com.hutoma.api.endpoints;

import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.logic.LogicTest;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Created by David MG on 25/07/2016.
 */
@Path("/admin")
public class EndpointTryout {

    @GET
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.TEXT_HTML)
    public String testOut() {
        return "no guice";
    }
}
