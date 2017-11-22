package com.hutoma.api.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Health check endpoint.
 */
@Path("/health/")
public class HealthcheckEndpoint {

    @Path("ping")
    @GET
    public Response ping() {
        return Response.ok().build();
    }
}
