package com.hutoma.api.endpoints;

import com.hutoma.api.access.RateKey;
import com.hutoma.api.access.RateLimit;
import com.hutoma.api.access.Role;
import com.hutoma.api.access.Secured;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.IntentLogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidateParameters;

import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by David MG on 05/10/2016.
 */
@RateLimit(RateKey.QuickRead)
@Path("/intents/")
public class IntentsEndpoint {

    private final IntentLogic intentLogic;
    private final JsonSerializer serializer;

    @Inject
    public IntentsEndpoint(IntentLogic intentLogic, JsonSerializer serializer) {
        this.intentLogic = intentLogic;
        this.serializer = serializer;
    }

    @GET
    @Path("{aiid}")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    public Response getIntents(
            @Context final ContainerRequestContext requestContext) {
        ApiResult result = this.intentLogic.getIntents(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext));
        return result.getResponse(this.serializer).build();
    }

    @POST
    @Path("{aiid}/csv")
    @Secured({Role.ROLE_FREE, Role.ROLE_PLAN_1, Role.ROLE_PLAN_2, Role.ROLE_PLAN_3, Role.ROLE_PLAN_4})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ValidateParameters({APIParameter.DevID, APIParameter.AIID})
    public Response bulkImportFromCsv(
            @Context final ContainerRequestContext requestContext,
            @FormDataParam("file") InputStream uploadedInputStream) {
        final ApiResult result = this.intentLogic.bulkImportFromCsv(
                ParameterFilter.getDevid(requestContext),
                ParameterFilter.getAiid(requestContext),
                uploadedInputStream);
        return result.getResponse(this.serializer).build();
    }
}
