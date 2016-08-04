package com.hutoma.api.endpoints;

import com.hutoma.api.auth.Role;
import com.hutoma.api.auth.Secured;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.logic.AdminLogic;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 04/08/2016.
 */
@Path("/ai/")
public class AIEndpoint {

    @Context
    AILogic aiLogic;

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkYzSkvzcRKVaAAAAAP__.kkftTodFfH_kRQANoqT1B96BslSHu1VzM5VC_p6bBcA" http://localhost:8080/api/
    @POST
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String createAI(
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
        return aiLogic.createAI(securityContext, devid, name, description, is_private, deep_learning_error, deep_learning_status, shallow_learning_status, status);
    }

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8W5Brq5KOkrFpUlAkYzSkvzcRKVaAAAAAP__.kkftTodFfH_kRQANoqT1B96BslSHu1VzM5VC_p6bBcA" http://localhost:8080/api/
    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getAIs(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        return aiLogic.getAIs(securityContext, devid);
    }

    @Path("/{aiid}/")
    @GET
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public String getSingleAI(
            @Context SecurityContext securityContext,
            @DefaultValue("") @HeaderParam("_developer_id") String devid,
            @PathParam("aiid") String aiid) {
        return aiLogic.getSingleAI(securityContext, devid, aiid);
    }

    @DELETE
    @Secured({Role.ROLE_FREE,Role.ROLE_PLAN_1,Role.ROLE_PLAN_2,Role.ROLE_PLAN_3,Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{aiid}")
    public String deleteAI(
            @Context SecurityContext securityContext,
            @PathParam("aiid") String aiid,
            @DefaultValue("") @HeaderParam("_developer_id") String devid) {
        return aiLogic.deleteAI(securityContext, aiid, devid);
    }
}
