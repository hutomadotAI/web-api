package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AIDomainLogic;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 05/08/2016.
 */
@Path("/ai/domain")
@RateLimit(RateKey.QuickRead)
public class AIDomainEndpoint {

    AIDomainLogic aiDomainLogic;
    JsonSerializer serializer;

    @Inject
    public AIDomainEndpoint(AIDomainLogic aiDomainLogic, JsonSerializer serializer) {
        this.aiDomainLogic = aiDomainLogic;
        this.serializer = serializer;
    }

    @GET
    @Secured( {Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomains(
        @Context SecurityContext securityContext) {
        ApiResult result = this.aiDomainLogic.getDomains(securityContext);
        return result.getResponse(this.serializer).build();
    }
}