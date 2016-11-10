package com.hutoma.api.endpoints;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.logic.AILogic;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidatePost;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by pedrotei on 08/11/16.
 */
@Path("/aiservices")
public class AIServicesStatusEndpoint {

    private final AILogic aiLogic;
    private final JsonSerializer serializer;

    @Inject
    public AIServicesStatusEndpoint(final AILogic aiLogic, final JsonSerializer serializer) {
        this.aiLogic = aiLogic;
        this.serializer = serializer;
    }

    @Path("{aiid}/status")
    @POST
    @ValidatePost({APIParameter.AiStatusJson})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStatus(
            @Context final SecurityContext securityContext,
            @Context final ContainerRequestContext requestContext) {
        ApiResult result = this.aiLogic.updateAIStatus(
                securityContext,
                ParameterFilter.getAiStatus(requestContext));
        return result.getResponse(this.serializer).build();
    }
}
