package com.hutoma.api.endpoints;

import com.hutoma.api.logic.FacebookIntegrationLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidatePost;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
            @Context final ContainerRequestContext requestContext,
            @DefaultValue("") @QueryParam("hub.mode") String mode,
            @DefaultValue("") @QueryParam("hub.challenge") String challenge,
            @DefaultValue("") @QueryParam("hub.verify_token") String verifyToken) {
        return this.facebookIntegrationLogic.verify(mode, challenge, verifyToken);
    }

    @POST
    @Path("facebook")
    @Produces(MediaType.APPLICATION_JSON)
    @ValidatePost({APIParameter.FacebookNotification})
    public Response facebookWebhook(
            @Context final ContainerRequestContext requestContext) {
        return this.facebookIntegrationLogic.chatRequest(
                ParameterFilter.getFacebookNotification(requestContext));
    }

}
