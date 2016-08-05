package com.hutoma.api.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.logic.AIDomainLogic;
import hutoma.api.server.ai.api_root;
import hutoma.api.server.db.domain;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;

/**
 * Created by David MG on 05/08/2016.
 */
@Path("/ai/domain")
public class AIDomainEndpoint {

    @Context
    AIDomainLogic aiDomainLogic;

    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getDomains(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        return aiDomainLogic.getDomains(securityContext, devid);
    }
}