package com.hutoma.api.endpoints;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.validation.APIParameter;
import com.hutoma.api.validation.ParameterFilter;
import com.hutoma.api.validation.ValidatePost;
import com.webcohesion.enunciate.metadata.rs.ResourceMethodSignature;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Endpoint to support updating AI status by backend services.
 */
@Path("/aiservices")
public class AIServicesStatusEndpoint {

    private final AIServices aiServices;
    private final JsonSerializer serializer;

    @Inject
    public AIServicesStatusEndpoint(final AIServices aiServices, final JsonSerializer serializer) {
        this.aiServices = aiServices;
        this.serializer = serializer;
    }

    /**
     * Update the AI status.
     * @param requestContext the request context
     * @return the result of the status update operation
     */
    @Path("{aiid}/status")
    @POST
    @ValidatePost({APIParameter.AiStatusJson})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Failed to update the status.")
    })
    @ResourceMethodSignature(
            input = AiStatus.class,
            output = ApiResult.class
    )
    public
    @TypeHint(ApiResult.class)
    Response updateStatus(
            @Context final ContainerRequestContext requestContext) {
        ApiResult result = this.aiServices.updateAIStatus(
                ParameterFilter.getAiStatus(requestContext));
        return result.getResponse(this.serializer).build();
    }
}
