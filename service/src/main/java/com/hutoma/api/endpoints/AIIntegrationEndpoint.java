package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AIIntegrationLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.hutoma.api.validation.ValidatePost;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Andrea MG on 30/09/2016.
 */
@Path("/ai")
@RateLimit(RateKey.QuickRead)
@ValidateParameters({APIParameter.DevID})
public class AIIntegrationEndpoint {

    AIIntegrationLogic aiIntegrationLogic;
    JsonSerializer serializer;

    @Inject
    public AIIntegrationEndpoint(AIIntegrationLogic aiIntegrationLogic, JsonSerializer serializer) {
        this.aiIntegrationLogic = aiIntegrationLogic;
        this.serializer = serializer;
    }

    @GET
    @Path("integration")
    @ValidateParameters({APIParameter.DevID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIntegrations(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiIntegrationLogic.getIntegrations(
                ParameterFilter.getDevid(requestContext)
        );
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("{aiid}/facebook/connect")
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost({APIParameter.FacebookConnect})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response facebookConnect(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiIntegrationLogic.facebookConnect(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getFacebookConnect(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("{aiid}/facebook")
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response facebookState(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiIntegrationLogic.getFacebookState(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("{aiid}/facebook")
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response facebookAction(
            @QueryParam("action") String action,
            @QueryParam("id") String pageId,
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiIntegrationLogic.facebookAction(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                action, pageId);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("{aiid}/facebook/custom")
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFacebookCustomisations(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiIntegrationLogic.getFacebookCustomisation(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("{aiid}/facebook/custom")
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost({APIParameter.FacebookCustomisations})
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setFacebookCustomisations(
            @Context ContainerRequestContext requestContext
    ) {
        ApiResult result = this.aiIntegrationLogic.setFacebookCustomisation(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getFacebookCustomisations(requestContext));
        return result.getResponse(this.serializer).build();
    }
}