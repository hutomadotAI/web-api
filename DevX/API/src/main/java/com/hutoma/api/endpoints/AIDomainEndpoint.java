package com.hutoma.api.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AIDomainLogic;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.db.domain;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;

/**
 * Created by David MG on 05/08/2016.
 */
@Path("/ai/domain")
public class AIDomainEndpoint {

    AIDomainLogic aiDomainLogic;
    JsonSerializer serializer;

    @Inject
    public AIDomainEndpoint(AIDomainLogic aiDomainLogic, JsonSerializer serializer) {
        this.aiDomainLogic = aiDomainLogic;
        this.serializer = serializer;
    }

    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDomains(
            @Context SecurityContext securityContext) {
        ApiResult result = aiDomainLogic.getDomains(securityContext);
        return result.getResponse(serializer).build();
    }
}