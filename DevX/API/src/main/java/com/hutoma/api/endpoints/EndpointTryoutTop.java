package com.hutoma.api.endpoints;

import com.hutoma.api.logic.AdminLogic;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Created by David MG on 25/07/2016.
 */
@Path("/")
public class EndpointTryoutTop {

    @Context
    AdminLogic adminLogic;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String testOut() {
        return "<h2>Hutoma API</h2>";
    }
}
