package com.hutoma.api.endpoints;

import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AdminLogic;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
            @DefaultValue("ROLE_FREE") @QueryParam("role") String securityRole,
            @DefaultValue("") @QueryParam("username") String username,
            @DefaultValue("") @QueryParam("email") String email,
            @DefaultValue("") @QueryParam("password") String password,
            @DefaultValue("") @QueryParam("password_salt") String passwordSalt,
            @DefaultValue("") @QueryParam("first_name") String firstName,
            @DefaultValue("") @QueryParam("last_name") String lastName,
            @DefaultValue("1") @QueryParam("plan_id") int planId) {

        ApiResult result = this.adminLogic.createDev(
                securityRole,
                username,
                email,
                password,
                passwordSalt,
                firstName,
                lastName,
                planId
        );
        return result.getResponse(this.serializer).build();
    }

    // curl -X DELETE -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJTY4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c" http://localhost:8080/api/admin?id=test2
    @DELETE
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeveloper(
            @DefaultValue("") @QueryParam("devid") String devId) {
        ApiResult result = this.adminLogic.deleteDev(devId);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("{devid}/DevToken")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToken(
            @DefaultValue("") @PathParam("devid") String devId) {
        ApiResult result = this.adminLogic.getDevToken(devId);
        return result.getResponse(this.serializer).build();
    }
}
