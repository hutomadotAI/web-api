package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.MeshLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


@RateLimit(RateKey.QuickRead)
@Path("/ai/")
public class MeshEndpoint {

    private final MeshLogic meshLogic;
    private final JsonSerializer serializer;

    @Inject
    public MeshEndpoint(MeshLogic meshLogic, JsonSerializer serializer) {
        this.meshLogic = meshLogic;
        this.serializer = serializer;
    }

    @GET
    @Path("{aiid}/mesh")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    public Response getMesh(
            @Context final SecurityContext securityContext,
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.meshLogic.getMesh(securityContext,
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }


    @POST
    @Path("{aiid}/mesh/{aiid_mesh}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.AIID_MESH})
    public Response addMesh(
            @Context final SecurityContext securityContext,
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.meshLogic.addMesh(securityContext,
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getAiidMesh(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @DELETE
    @Path("{aiid}/mesh/{aiid_mesh}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.AIID_MESH})
    public Response deleteSingleMesh(
            @Context final SecurityContext securityContext,
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.meshLogic.deleteSingleMesh(securityContext,
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getAiidMesh(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @DELETE
    @Path("{aiid}/mesh")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    public Response deleteAllMesh(
            @Context final SecurityContext securityContext,
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.meshLogic.deleteAllMesh(securityContext,
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }


}
