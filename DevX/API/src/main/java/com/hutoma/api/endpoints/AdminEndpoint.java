package com.hutoma.api.endpoints;

import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AdminLogic;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by David MG on 28/07/2016.
 */
@Path("/admin/")
@Secured({Role.ROLE_ADMIN})
public class AdminEndpoint {

    AdminLogic adminLogic;
    JsonSerializer serializer;

    @Inject
    public AdminEndpoint(AdminLogic adminLogic, JsonSerializer serializer) {
        this.adminLogic = adminLogic;
        this.serializer = serializer;
    }

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8Y4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c" http://localhost:8080/api/admin?id=test&role=ROLE_CLIENTONLY
    @POST
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminPost(
            @Context SecurityContext securityContext,
            @DefaultValue("ROLE_FREE") @QueryParam("role") String securityRole,
            @DefaultValue("") @QueryParam("devid") String developerID,
            @DefaultValue("") @QueryParam("username") String username,
            @DefaultValue("") @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("password") String password,
            @DefaultValue("") @QueryParam("password_salt") String passwordSalt,
            @DefaultValue("") @QueryParam("name")  String name,
            @DefaultValue("") @QueryParam("attempt") String attempt,
            @DefaultValue("") @QueryParam("dev_token") String developerToken,
            @DefaultValue("1") @QueryParam("plan_id") int planId,
            @DefaultValue("") @QueryParam("dev_id") String dev_id) {

        ApiResult result = adminLogic.createDev(securityContext, securityRole,
                developerID,
                username,
                email,
                password,
                passwordSalt,
                name,
                attempt,
                developerToken,
                planId,
                dev_id);
        return result.getResponse(serializer).build();
    }

    // curl -X DELETE -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJTY4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c" http://localhost:8080/api/admin?id=test2
    @DELETE
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete_dev(
            @Context SecurityContext securityContext,
            @DefaultValue("") @QueryParam("devid") String devid) {
        ApiResult result = adminLogic.deleteDev(securityContext, devid);
        return result.getResponse(serializer).build();
    }

}
