package com.hutoma.api.endpoints;

import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ValidateParameters;
import com.hutoma.api.validation.ParameterFilter;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 04/08/2016.
 */
@Path("/ai/")
public class AIEndpoint {

    AILogic aiLogic;
    JsonSerializer serializer;

    @Inject
    public AIEndpoint(AILogic aiLogic, JsonSerializer serializer) {
        this.aiLogic = aiLogic;
        this.serializer = serializer;
    }
    
    @POST
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIName, APIParameter.AIDescription})
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAI(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("false") @QueryParam("is_private") boolean is_private,
            @DefaultValue("0.0") @QueryParam("deep_learning_error") double deep_learning_error,
            @DefaultValue("0") @QueryParam("deep_learning_status") int deep_learning_status,
            @DefaultValue("0") @QueryParam("shallow_learning_status") int shallow_learning_status,
            @DefaultValue("0") @QueryParam("status") int status)
    {
        ApiResult result = aiLogic.createAI(securityContext, devid,
                ParameterFilter.getAiName(requestContext), ParameterFilter.getAiDescription(requestContext),
                is_private, deep_learning_error, deep_learning_status, shallow_learning_status, status);
        return result.getResponse(serializer).build();
    }

    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAIs(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = aiLogic.getAIs(securityContext, devid);
        return result.getResponse(serializer).build();
    }

    @Path("{aiid}")
    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSingleAI(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = aiLogic.getSingleAI(securityContext, devid,
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(serializer).build();
    }

    @Path("{aiid}")
    @DELETE
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAI(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = aiLogic.deleteAI(securityContext, devid,
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(serializer).build();
    }
}
