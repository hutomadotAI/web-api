package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AIIntegrationLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ValidateParameters;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by Andrea MG on 30/09/2016.
 */
@Path("/ai/integration")
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
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIntegrations(
            @Context SecurityContext securityContext) {
        ApiResult result = this.aiIntegrationLogic.getIntegrations(securityContext);
        return result.getResponse(this.serializer).build();
    }
}