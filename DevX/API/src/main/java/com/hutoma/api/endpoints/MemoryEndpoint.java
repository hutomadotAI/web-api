package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.MemoryLogic;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;


@Path("/ai/")
@RateLimit(RateKey.None)
public class MemoryEndpoint {

    SecurityContext securityContext;
    JsonSerializer serializer;
    MemoryLogic memoryLogic;

    @Inject
    public MemoryEndpoint(SecurityContext securityContext, JsonSerializer serializer, MemoryLogic memoryLogic) {
        this.securityContext = securityContext;
        this.serializer = serializer;
        this.memoryLogic = memoryLogic;
    }

    /**
     * This API returns a list of all variables currently in memory for a given user
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id the dev id
     * @param aiid the ai id
     * @param uid the user id
     * @return a json response containing the list of all variables in memory including expired variables
     * @throws IOException
     * @throws InterruptedException
     */
    @GET
    @Path("{aiid}/{userid}/memory")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariables(@Context SecurityContext securityContext,
                                 @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                 @PathParam("aiid") String aiid,
                                 @PathParam("userid") String uid
    ) {
        ApiResult result = memoryLogic.getVariables(securityContext, dev_id, aiid, uid);
        return result.getResponse(serializer).build();
    }

    /**
     *  Get a single variable value from the AI memory
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id the dev id
     * @param aiid the ai id
     * @param uid the user id
     * @param variable the variable to retrieve
     * @return a json object with the variable
     * @throws IOException
     * @throws InterruptedException
     */
    @GET
    @Path("{aiid}/{userid}/memory/{variable}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariable (@Context SecurityContext securityContext,
                                 @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                 @PathParam("aiid") String aiid,
                                 @PathParam("userid") String uid,
                                 @PathParam("variable") String variable
    ) {
        ApiResult result = memoryLogic.getSingleVariable(securityContext, dev_id, aiid, uid, variable);
        return result.getResponse(serializer).build();
    }

    /**
     * Saves a new variable in memory. If the variable exists already it will update it.
     *
     * @param securityContext the security context
     * @param dev_id the dev id
     * @param aiid the ai id
     * @param uid the user id
     * @param variable variable name
     * @param value variable value
     * @param n_prompts number of times the AI will prompt for this variable
     * @param expires_seconds nubmer of seconds after which the variable is purged from memory
     * @param label a label to remember to which categoty the variable belongs to (ex. city, family, etc)
     * @return it returns 500 in case of error. 200 otherwise
     * @throws IOException
     * @throws InterruptedException
     */
    @PUT
    @Path("{aiid}/{userid}/memory/{variable}/{value}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response setVariable (@Context SecurityContext securityContext,
                                 @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                 @PathParam("aiid") String aiid,
                                 @PathParam("userid") String uid,
                                 @PathParam("variable") String variable,
                                 @PathParam("value") String value,
                                 @DefaultValue("5") @QueryParam("n_prompts") int n_prompts,
                                 @DefaultValue("300") @QueryParam("expires") int expires_seconds,
                                 @DefaultValue("300") @QueryParam("label") String label
    ) {
        ApiResult result = memoryLogic.setVariable(securityContext, dev_id, aiid, uid, variable, value, n_prompts, expires_seconds, label);
        return result.getResponse(serializer).build();
    }

    /**
     * Removes a variable from the AI memory
     *
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id the dev id
     * @param aiid the ai id
     * @param uid the user id
     * @param variable the variable to return
     * @return success
     * @throws IOException
     * @throws InterruptedException
     */
    @DELETE
    @Path("{aiid}/{userid}/memory/{variable}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response del_variable (@Context SecurityContext securityContext,
                                  @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                  @PathParam("aiid") String aiid,
                                  @PathParam("userid") String uid,
                                  @PathParam("variable") String variable
    ) {
        ApiResult result = memoryLogic.delVariable(securityContext, dev_id, aiid, uid, variable);
        return result.getResponse(serializer).build();
    }

    /**
     * Removes all  variables from the AI memory for a specific user
     *
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id the dev id
     * @param aiid the ai id
     * @param uid the user id
     * @return success
     * @throws IOException
     * @throws InterruptedException
     */
    @DELETE
    @Path("{aiid}/{userid}/memory")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response del_all_variable (@Context SecurityContext securityContext,
                                      @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                      @PathParam("aiid") String aiid,
                                      @PathParam("userid") String uid

    ){
        ApiResult result = memoryLogic.removeAllUserVariables(securityContext, dev_id, aiid, uid);
        return result.getResponse(serializer).build();
    }

    /**
     * Removes all variables for all users associated to a specific AI
     *
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id the dev id
     * @param aiid the ai id
     * @return success
     * @throws IOException
     * @throws InterruptedException
     */
    @DELETE
    @Path("{aiid}/memory")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response del_all_variable (@Context SecurityContext securityContext,
                                      @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                      @PathParam("aiid") String aiid

    ) {
        ApiResult result = memoryLogic.removeAllAiVariables(securityContext, dev_id, aiid);
        return result.getResponse(serializer).build();
    }
}
