package com.hutoma.api.endpoints;

import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AdminLogic;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Administrative endpoint.
 */
@Path("/admin/")
@Secured({Role.ROLE_ADMIN})
public class AdminEndpoint {

    private final AdminLogic adminLogic;
    private final JsonSerializer serializer;

    @Inject
    public AdminEndpoint(final AdminLogic adminLogic, final JsonSerializer serializer) {
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
            @DefaultValue("") @QueryParam("devid") UUID devId) {
        ApiResult result = this.adminLogic.deleteDev(devId);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("{devid}/devToken")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToken(
            @DefaultValue("") @PathParam("devid") UUID devId) {
        ApiResult result = this.adminLogic.getDevToken(devId);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("user")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserFromUsername(
            @DefaultValue("") @QueryParam("username") String username) {
        ApiResult result = this.adminLogic.getUser(username);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("user/exists")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(
            @QueryParam("username") String username,
            @DefaultValue("true") @QueryParam("checkEmail") boolean checkEmail) {
        ApiResult result = this.adminLogic.doesUserExist(username, checkEmail);
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("{devid}/loginAttempts")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(
            @PathParam("devid") String devId,
            @FormParam("loginAttempts") String loginAttempts) {
        ApiResult result = this.adminLogic.updateLoginAttempts(devId, loginAttempts);
        return result.getResponse(this.serializer).build();
    }

    @PUT
    @Path("password")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserPassword(
            @FormParam("user_id") int userId,
            @FormParam("password") String password,
            @FormParam("password_salt") String passwordSalt) {
        ApiResult result = this.adminLogic.updateUserPassword(userId, password, passwordSalt);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("reset_token")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response isResetTokenValid(
            @QueryParam("token") String token) {
        ApiResult result = this.adminLogic.isPasswordResetTokenValid(token);
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("reset_token/user")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserIdForResetToken(
            @QueryParam("token") String token) {
        ApiResult result = this.adminLogic.getUserIdForResetToken(token);
        return result.getResponse(this.serializer).build();
    }

    @DELETE
    @Path("reset_token")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteResetToken(
            @QueryParam("token") String token) {
        ApiResult result = this.adminLogic.deletePasswordResetToken(token);
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("reset_token")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertResetToken(
            @FormParam("user_id") int userId,
            @FormParam("token") String token) {
        ApiResult result = this.adminLogic.insertPasswordResetToken(userId, token);
        return result.getResponse(this.serializer).build();
    }
}
