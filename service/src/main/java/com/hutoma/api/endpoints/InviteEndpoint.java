package com.hutoma.api.endpoints;

import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.InviteLogic;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Endpoints for invite codes.
 */
@Path("/invite/")
public class InviteEndpoint {
    InviteLogic inviteLogic;
    JsonSerializer serializer;

    @Inject
    public InviteEndpoint(InviteLogic inviteLogic, JsonSerializer serializer) {
        this.inviteLogic = inviteLogic;
        this.serializer = serializer;
    }

    @GET
    @Path("{code}")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response validToken(
            @Context SecurityContext securityContext,
            @DefaultValue ("") @PathParam("code") String code) {
        ApiResult result = this.inviteLogic.validCode(code);
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("{code}/redeem")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response redeemToken(
            @Context SecurityContext securityContext,
            @DefaultValue ("") @PathParam("code") String code,
            @DefaultValue("") @QueryParam("username") String username) {
        ApiResult result = this.inviteLogic.redeemCode(code, username);
        return result.getResponse(this.serializer).build();
    }
}
