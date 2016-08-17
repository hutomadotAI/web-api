package com.hutoma.api.endpoints;

import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.AdminLogic;

import javax.inject.Inject;
import javax.ws.rs.*;
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

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkYzSkvzcRKVaAAAAAP__.kkftTodFfH_kRQANoqT1B96BslSHu1VzM5VC_p6bBcA" http://localhost:8080/api/
    @POST
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAI(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @DefaultValue("") @QueryParam("name") String name,
            @DefaultValue("") @QueryParam("description") String description,
            @DefaultValue("false") @QueryParam("is_private") boolean is_private,
            @DefaultValue("0.0") @QueryParam("deep_learning_error") double deep_learning_error,
            @DefaultValue("0") @QueryParam("deep_learning_status") int deep_learning_status,
            @DefaultValue("0") @QueryParam("shallow_learning_status") int shallow_learning_status,
            @DefaultValue("0") @QueryParam("status") int status)
    {
        ApiResult result = aiLogic.createAI(securityContext, devid, name, description, is_private, deep_learning_error, deep_learning_status, shallow_learning_status, status);
        return result.getResponse(serializer).build();
    }

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkYzSkvzcRKVaAAAAAP__.kkftTodFfH_kRQANoqT1B96BslSHu1VzM5VC_p6bBcA" http://localhost:8080/api/
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSingleAI(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @PathParam("aiid") String aiid) {
        ApiResult result = aiLogic.getSingleAI(securityContext, devid, aiid);
        return result.getResponse(serializer).build();
    }

    @Path("{aiid}")
    @DELETE
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAI(
            @Context SecurityContext securityContext,
            @PathParam("aiid") String aiid,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        ApiResult result = aiLogic.deleteAI(securityContext, devid, aiid);
        return result.getResponse(serializer).build();
    }
}
