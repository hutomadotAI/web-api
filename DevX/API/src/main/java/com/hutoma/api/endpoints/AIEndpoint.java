package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.hutoma.api.validation.ValidatePost;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 04/08/2016.
 */
@Path("/ai/")
@RateLimit(RateKey.QuickRead)
public class AIEndpoint {

    private final AILogic aiLogic;
    private final JsonSerializer serializer;

    @Inject
    public AIEndpoint(AILogic aiLogic, JsonSerializer serializer) {
        this.aiLogic = aiLogic;
        this.serializer = serializer;
    }

    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID})
    @ValidatePost({APIParameter.AIName, APIParameter.AIDescription, APIParameter.AiConfidence,
            APIParameter.Timezone, APIParameter.Locale})
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAI(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext,
            @DefaultValue("false") @FormParam("is_private") boolean isPrivate,
            @DefaultValue("0") @FormParam("personality") int personality,
            @DefaultValue("0") @FormParam("voice") int voice) {
        ApiResult result = this.aiLogic.createAI(
                securityContext,
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiName(requestContext),
                ParameterFilter.getAiDescription(requestContext),
                isPrivate,
                personality,
                ParameterFilter.getAiConfidence(requestContext),
                voice,
                ParameterFilter.getLocale(requestContext),
                ParameterFilter.getTimezone(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @Path("{aiid}")
    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @ValidatePost({APIParameter.AIDescription, APIParameter.AiConfidence, APIParameter.Timezone,
            APIParameter.Locale})
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAI(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext,
            @DefaultValue("false") @FormParam("is_private") boolean isPrivate,
            @DefaultValue("0") @FormParam("personality") int personality,
            @DefaultValue("0") @FormParam("voice") int voice) {
        ApiResult result = this.aiLogic.updateAI(
                securityContext,
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getAiDescription(requestContext),
                isPrivate,
                personality,
                ParameterFilter.getAiConfidence(requestContext),
                voice,
                ParameterFilter.getLocale(requestContext),
                ParameterFilter.getTimezone(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.DevID}) // Although this is always checked need to add it to trigger the filter
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAIs(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.getAIs(
                securityContext,
                ParameterFilter.getDevid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @Path("{aiid}")
    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSingleAI(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.getSingleAI(
                securityContext,
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @Path("{aiid}")
    @DELETE
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @ValidateParameters({APIParameter.AIID})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAI(
            @Context SecurityContext securityContext,
            @Context ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.deleteAI(
                securityContext,
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }


}
