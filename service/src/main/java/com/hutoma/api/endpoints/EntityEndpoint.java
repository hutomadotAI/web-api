package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.EntityLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;
import com.hutoma.api.validation.ValidatePost;

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

/**
 * Created by David MG on 05/10/2016.
 */
@RateLimit(RateKey.QuickRead)
@Path("/entity/")
public class EntityEndpoint {

    private final EntityLogic entityLogic;
    private final JsonSerializer serializer;

    @Inject
    public EntityEndpoint(final EntityLogic entityLogic, final JsonSerializer serializer) {
        this.entityLogic = entityLogic;
        this.serializer = serializer;
    }

    @GET
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.EntityName})
    public Response getEntity(
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.entityLogic.getEntity(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getEntityName(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.EntityName})
    @ValidatePost({APIParameter.EntityJson})
    public Response postEntity(
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.entityLogic.writeEntity(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getEntityName(requestContext),
                ParameterFilter.getEntity(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @DELETE
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.EntityName})
    public Response deleteEntity(
            @Context final ContainerRequestContext requestContext) {
        final ApiResult result = this.entityLogic.deleteEntity(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getEntityName(requestContext));
        return result.getResponse(this.serializer).build();
    }
}
