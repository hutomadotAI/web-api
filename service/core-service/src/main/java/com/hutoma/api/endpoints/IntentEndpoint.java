package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.IntentLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.hutoma.api.validation.ValidatePost;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by David MG on 05/10/2016.
 */
@Path("/intent/")
public class IntentEndpoint {

    private final IntentLogic intentLogic;
    private final JsonSerializer serializer;

    @Inject
    public IntentEndpoint(IntentLogic intentLogic, JsonSerializer serializer) {
        this.intentLogic = intentLogic;
        this.serializer = serializer;
        // Allow nulls on Intent serialization
        this.serializer.allowNullsOnSerialization();
    }

    @GET
    @Path("{aiid}")
    @RateLimit(RateKey.QuickRead)
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.IntentName})
    public Response getIntents(
            @Context final ContainerRequestContext requestContext) {
        ApiResult result = this.intentLogic.getIntent(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getIntentName(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @POST
    @RateLimit(RateKey.SaveResource)
    @Path("{aiid}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost({APIParameter.IntentJson})
    public Response postIntent(
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.intentLogic.createIntent(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getIntent(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @RateLimit(RateKey.SaveResource)
    @Path("{aiid}/{intent_name}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    @ValidatePost({APIParameter.IntentJson})
    public Response updateIntent(
            @PathParam("intent_name") String prevIntentName,
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.intentLogic.updateIntent(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getIntent(requestContext),
                prevIntentName);
        return result.getResponse(this.serializer).build();
    }


    @DELETE
    @RateLimit(RateKey.SaveResource)
    @Path("{aiid}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID, APIParameter.IntentName})
    public Response deleteIntent(
            @Context final ContainerRequestContext requestContext) {

        final ApiResult result = this.intentLogic.deleteIntent(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                ParameterFilter.getIntentName(requestContext));
        return result.getResponse(this.serializer).build();
    }
}
