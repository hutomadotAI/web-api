package com.hutoma.api.endpoints;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiServerAcknowledge;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.logic.AIServicesLogic;
import com.hutoma.api.validation.ControllerParameter;
import com.hutoma.api.validation.ControllerParameterFilter;
import com.hutoma.api.validation.ValidateControllerPost;
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
public class AIServicesEndpoint {

    private final AIServicesLogic aiServices;
    private final JsonSerializer serializer;

    @Inject
    public AIServicesEndpoint(final AIServicesLogic aiServices, final JsonSerializer serializer) {
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
    @ValidateControllerPost({ControllerParameter.AiStatusJson})
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
                ControllerParameterFilter.getAiStatus(requestContext));
        return result.getResponse(this.serializer).build();
    }

    /**
     * Register backend servers with the API/Controller.
     * @param requestContext the request context
     * @return success
     */
    @Path("register")
    @POST
    @ValidateControllerPost({ControllerParameter.ServerRegistration})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Registration failed.")
    })
    @ResourceMethodSignature(
            input = ServerRegistration.class,
            output = ApiServerAcknowledge.class
    )
    public
    @TypeHint(ApiResult.class)
    Response registerBackend(
            @Context final ContainerRequestContext requestContext) {
        ApiResult result = this.aiServices.registerServer(
                ControllerParameterFilter.getServerRegistration(requestContext));
        return result.getResponse(this.serializer).build();
    }

    /**
     * Update the affinity table; tell the controller which AIs a backend has in memory
     * @param requestContext the request context
     * @return the result
     */
    @Path("affinity")
    @POST
    @ValidateControllerPost({ControllerParameter.ServerAffinity})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Affinity update failed.")
    })
    @ResourceMethodSignature(
            input = ServerAffinity.class,
            output = ApiResult.class
    )
    public
    @TypeHint(ApiResult.class)
    Response updateAffinity(
            @Context final ContainerRequestContext requestContext) {
        ApiResult result = this.aiServices.updateAffinity(
                ControllerParameterFilter.getServerAffinity(requestContext));
        return result.getResponse(this.serializer).build();
    }
}
