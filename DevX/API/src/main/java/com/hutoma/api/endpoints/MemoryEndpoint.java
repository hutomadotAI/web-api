package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.MemoryLogic;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;


@Path("/ai/")
@RateLimit(RateKey.None)
public class MemoryEndpoint {

    private final JsonSerializer serializer;
    private final MemoryLogic memoryLogic;
    private final SecurityContext securityContext;

    @Inject
    public MemoryEndpoint(final SecurityContext securityContext, final JsonSerializer serializer,
                          final MemoryLogic memoryLogic) {
        this.securityContext = securityContext;
        this.serializer = serializer;
        this.memoryLogic = memoryLogic;
    }

    /**
     * This API returns a list of all variables currently in memory for a given user
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id          the dev id
     * @param aiid            the ai id
     * @param uid             the user id
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
        ApiResult result = this.memoryLogic.getVariables(securityContext, dev_id, aiid, uid);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Get a single variable value from the AI memory
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id          the dev id
     * @param aiid            the ai id
     * @param uid             the user id
     * @param variable        the variable to retrieve
     * @return a json object with the variable
     * @throws IOException
     * @throws InterruptedException
     */
    @GET
    @Path("{aiid}/{userid}/memory/{variable}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariable(@Context SecurityContext securityContext,
                                @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                @PathParam("aiid") String aiid,
                                @PathParam("userid") String uid,
                                @PathParam("variable") String variable
    ) {
        ApiResult result = this.memoryLogic.getSingleVariable(securityContext, dev_id, aiid, uid, variable);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Saves a new variable in memory. If the variable exists already it will update it.
     * @param securityContext the security context
     * @param dev_id          the dev id
     * @param aiid            the ai id
     * @param uid             the user id
     * @param variable        variable name
     * @param value           variable value
     * @param n_prompts       number of times the AI will prompt for this variable
     * @param expires_seconds nubmer of seconds after which the variable is purged from memory
     * @param label           a label to remember to which categoty the variable belongs to (ex. city, family, etc)
     * @return it returns 500 in case of error. 200 otherwise
     * @throws IOException
     * @throws InterruptedException
     */
    @PUT
    @Path("{aiid}/{userid}/memory/{variable}/{value}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response setVariable(@Context SecurityContext securityContext,
                                @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                @PathParam("aiid") String aiid,
                                @PathParam("userid") String uid,
                                @PathParam("variable") String variable,
                                @PathParam("value") String value,
                                @DefaultValue("5") @QueryParam("n_prompts") int n_prompts,
                                @DefaultValue("300") @QueryParam("expires") int expires_seconds,
                                @DefaultValue("300") @QueryParam("label") String label
    ) {
        ApiResult result = this.memoryLogic.setVariable(securityContext, dev_id, aiid, uid, variable, value, n_prompts, expires_seconds, label);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Removes a variable from the AI memory
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id          the dev id
     * @param aiid            the ai id
     * @param uid             the user id
     * @param variable        the variable to return
     * @return success
     * @throws IOException
     * @throws InterruptedException
     */
    @DELETE
    @Path("{aiid}/{userid}/memory/{variable}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response del_variable(@Context SecurityContext securityContext,
                                 @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                 @PathParam("aiid") String aiid,
                                 @PathParam("userid") String uid,
                                 @PathParam("variable") String variable
    ) {
        ApiResult result = this.memoryLogic.delVariable(securityContext, dev_id, aiid, uid, variable);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Removes all  variables from the AI memory for a specific user
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id          the dev id
     * @param aiid            the ai id
     * @param uid             the user id
     * @return success
     * @throws IOException
     * @throws InterruptedException
     */
    @DELETE
    @Path("{aiid}/{userid}/memory")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response del_all_variable(@Context SecurityContext securityContext,
                                     @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                     @PathParam("aiid") String aiid,
                                     @PathParam("userid") String uid

    ) {
        ApiResult result = this.memoryLogic.removeAllUserVariables(securityContext, dev_id, aiid, uid);
        return result.getResponse(this.serializer).build();
    }

    /**
     * Removes all variables for all users associated to a specific AI
     * @param securityContext the security context that contains the token from where we extract the dev id
     * @param dev_id          the dev id
     * @param aiid            the ai id
     * @return success
     * @throws IOException
     * @throws InterruptedException
     */
    @DELETE
    @Path("{aiid}/memory")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    public Response del_all_variable(@Context SecurityContext securityContext,
                                     @DefaultValue("") @HeaderParam("_developer_id") String dev_id,
                                     @PathParam("aiid") String aiid

    ) {
        ApiResult result = this.memoryLogic.removeAllAiVariables(securityContext, dev_id, aiid);
        return result.getResponse(this.serializer).build();
    }
}
