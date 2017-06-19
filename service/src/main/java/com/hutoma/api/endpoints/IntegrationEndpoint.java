package com.hutoma.api.endpoints;

import com.hutoma.api.logic.FacebookIntegrationLogic;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/integration")
public class IntegrationEndpoint {

    FacebookIntegrationLogic facebookIntegrationLogic;

    @Inject
    public IntegrationEndpoint(final FacebookIntegrationLogic facebookIntegrationLogic) {
        this.facebookIntegrationLogic = facebookIntegrationLogic;
    }

    @GET
    @Path("facebook")
    public Response facebookWebhookVerify(
            @DefaultValue("") @QueryParam("hub.mode") String mode,
            @DefaultValue("") @QueryParam("hub.challenge") String challenge,
            @DefaultValue("") @QueryParam("hub.verify_token") String verifyToken) {
        return this.facebookIntegrationLogic.verify(mode, challenge, verifyToken);
    }

    @POST
    @Path("facebook")
    public Response facebookWebhook() {
        return Response.ok().build();
    }

}
