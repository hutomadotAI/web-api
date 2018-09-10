package com.hutoma.api.endpoints;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiServerEndpoint;
import com.hutoma.api.containers.ApiServerEndpointMulti;
import com.hutoma.api.containers.ApiServerTrackerInfoMap;
import com.hutoma.api.logic.ControllerLogic;
import com.hutoma.api.validation.ControllerParameter;
import com.hutoma.api.validation.ControllerParameterFilter;
import com.hutoma.api.validation.ValidateControllerParameters;
import com.hutoma.api.validation.ValidateControllerPost;
import com.webcohesion.enunciate.metadata.rs.ResourceMethodSignature;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;

import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Endpoint for obtaining backend server info from the controller.
 * The controller is responsible for managing the backends, so it has the information about the best routing for
 * training and chat requests.
 * This endpoint allows clients to obtain the routing information to determine which backend to (directly) connect to
 * for training and chatting.
 */
@Path("/controller")
public class ControllerEndpoint {

    private final ControllerLogic controllerLogic;
    private final JsonSerializer serializer;

    @Inject
    public ControllerEndpoint(final ControllerLogic controllerLogic, final JsonSerializer serializer) {
        this.controllerLogic = controllerLogic;
        this.serializer = serializer;
    }

    @GET
    @Path("{aiid}/training")
    @ValidateControllerParameters({ControllerParameter.AIID, ControllerParameter.ServerType,
            ControllerParameter.ServerLanguage, ControllerParameter.ServerVersion})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("for"), @QueryParam("serverType"), @QueryParam("serverLanguage"),
                    @QueryParam("serverVersion")},
            output = ApiServerEndpoint.class
    )
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpoint(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.controllerLogic.getBackendTrainingEndpoint(
                ControllerParameterFilter.getAiid(requestContext),
                ControllerParameterFilter.getBackendServerType(requestContext),
                ControllerParameterFilter.getBackendServerLanguage(requestContext),
                ControllerParameterFilter.getBackendServerVersion(requestContext));
        return result.getResponse(serializer).build();
    }

    @POST
    @Path("chatEndpoints")
    @ValidateControllerParameters({ControllerParameter.ServerType, ControllerParameter.ServerLanguage,
            ControllerParameter.ServerVersion})
    @ValidateControllerPost({ControllerParameter.ServerEndpointMulti})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("serverType"), @QueryParam("serverLanguage"), @QueryParam("serverVersion")},
            output = ApiServerEndpointMulti.class
    )
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServerEndpointMulti(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.controllerLogic.getBackendChatEndpointsMulti(
                ControllerParameterFilter.getBackendServerType(requestContext),
                ControllerParameterFilter.getBackendServerLanguage(requestContext),
                ControllerParameterFilter.getBackendServerVersion(requestContext),
                ControllerParameterFilter.getServerEndpointRequestMulti(requestContext));
        return result.getResponse(serializer).build();
    }

    @GET
    @Path("endpointMap")
    @ValidateControllerParameters({ControllerParameter.ServerType, ControllerParameter.ServerLanguage,
            ControllerParameter.ServerVersion})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("serverType"), @QueryParam("serverLanguage"), @QueryParam("serverVersion")},
            output = ApiServerTrackerInfoMap.class
    )
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpointMap(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.controllerLogic.getMap(
                ControllerParameterFilter.getBackendServerType(requestContext),
                ControllerParameterFilter.getBackendServerLanguage(requestContext),
                ControllerParameterFilter.getBackendServerVersion(requestContext)
        );
        return result.getResponse(serializer).build();
    }

    @POST
    @Path("queue")
    @ValidateControllerParameters({ControllerParameter.ServerType, ControllerParameter.ServerLanguage,
            ControllerParameter.ServerVersion})
    @StatusCodes({
            @ResponseCode(code = HttpURLConnection.HTTP_OK, condition = "Succeeded."),
            @ResponseCode(code = HttpURLConnection.HTTP_INTERNAL_ERROR, condition = "Internal error")
    })
    @ResourceMethodSignature(
            queryParams = {@QueryParam("serverType"), @QueryParam("serverLanguage"), @QueryParam("serverVersion")},
            output = ApiServerTrackerInfoMap.class
    )
    @Produces(MediaType.APPLICATION_JSON)
    public Response kickQueue(@Context ContainerRequestContext requestContext) {
        ApiResult result = this.controllerLogic.kickQueue(
                ControllerParameterFilter.getBackendServerType(requestContext),
                ControllerParameterFilter.getBackendServerLanguage(requestContext),
                ControllerParameterFilter.getBackendServerVersion(requestContext)
        );
        return result.getResponse(serializer).build();
    }
}
