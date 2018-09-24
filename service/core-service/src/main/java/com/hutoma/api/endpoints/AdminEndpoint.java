package com.hutoma.api.endpoints;

import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AdminLogic;
import com.hutoma.api.logic.IntentLogic;
import com.hutoma.api.logic.TrainingLogic;

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
    private final TrainingLogic trainingLogic;

    @Inject
    public AdminEndpoint(final AdminLogic adminLogic,
                         final TrainingLogic trainingLogic,
                         final JsonSerializer serializer) {
        this.adminLogic = adminLogic;
        this.trainingLogic = trainingLogic;
        this.serializer = serializer;
    }

    //curl -X POST -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNqqVgry93FVsgJT8Y4uvp5-SjpKxaVJQKHElNzMPKVaAAAAAP__.e-INR1D-L_sokTh9sZ9cBnImWI0n6yXXpDCmat1ca_c" http://localhost:8080/api/admin?id=test&role=ROLE_CLIENTONLY
    @POST
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminPost(
            @DefaultValue("ROLE_FREE") @QueryParam("role") String securityRole,
            @DefaultValue("1") @QueryParam("plan_id") int planId) {

        ApiResult result = this.adminLogic.createDev(
                securityRole,
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

    @POST
    @Path("regenerate_tokens")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response regenerateAllTokens(
            @DefaultValue("false") @QueryParam("dryrun") boolean dryrun
    ) {
        ApiResult result = this.adminLogic.regenerateTokens(null, dryrun);
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("{devid}/regenerate_token")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response regenerateUserToken(
            @PathParam("devid") String devId,
            @DefaultValue("false") @QueryParam("dryrun") boolean dryrun) {
        ApiResult result = this.adminLogic.regenerateTokens(devId, dryrun);
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("migration/{devid}/{aiid}")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response migrationRetrainBot(
            @PathParam("devid") String devId,
            @PathParam("aiid") String aiid) {
        ApiResult result = this.trainingLogic.updateTraining(
                UUID.fromString(devId), UUID.fromString(aiid));
        return result.getResponse(this.serializer).build();
    }

    @GET
    @Path("migration/{devid}/{aiid}")
    @Secured({Role.ROLE_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response migrationBotStatus(
            @PathParam("devid") String devId,
            @PathParam("aiid") String aiid) {
        ApiResult result = this.trainingLogic.getAiTrainingStatus(
                UUID.fromString(devId), UUID.fromString(aiid));
        return result.getResponse(this.serializer).build();
    }
}
