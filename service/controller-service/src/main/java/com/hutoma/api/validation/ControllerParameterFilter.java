package com.hutoma.api.validation;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerEndpointRequestMulti;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.logging.AiServiceStatusLogger;

import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

public class ControllerParameterFilter extends ValidationBase {

    static final String SERVER_URL = "server_url";

    static final String SERVER_TYPE = "server_type";
    static final String AI_LIST = "ai_list";
    static final String SERVER_SESSION_ID = "server_session_id";
    static final String ENDPOINT_REQUEST_LIST = "endpoint_request_list";

    protected final AiServiceStatusLogger logger;
    protected final Tools tools;
    protected final JsonSerializer serializer;

    @Inject
    public ControllerParameterFilter(final AiServiceStatusLogger logger, final Tools tools,
                                     final JsonSerializer serializer) {
        this.logger = logger;
        this.tools = tools;
        this.serializer = serializer;
    }

    public static UUID getAiid(final ContainerRequestContext requestContext) {
        return (UUID) requestContext.getProperty(ControllerParameter.AIID.toString());
    }

    public static AiStatus getAiStatus(final ContainerRequestContext requestContext) {
        return (AiStatus) requestContext.getProperty(ControllerParameter.AiStatusJson.toString());
    }

    public static ServerRegistration getServerRegistration(final ContainerRequestContext requestContext) {
        return (ServerRegistration) requestContext.getProperty(ControllerParameter.ServerRegistration.toString());
    }

    public static ServerAffinity getServerAffinity(final ContainerRequestContext requestContext) {
        return (ServerAffinity) requestContext.getProperty(ControllerParameter.ServerAffinity.toString());
    }

    public static RequestFor getRequestFor(final ContainerRequestContext requestContext) {
        return (RequestFor) requestContext.getProperty(ControllerParameter.RequestFor.toString());
    }

    public static BackendServerType getBackendServerType(final ContainerRequestContext requestContext) {
        return (BackendServerType) requestContext.getProperty(ControllerParameter.ServerType.toString());
    }

    public static ServerEndpointRequestMulti getServerEndpointRequestMulti(final ContainerRequestContext requestContext) {
        return (ServerEndpointRequestMulti) requestContext.getProperty(ControllerParameter.ServerEndpointMulti.toString());
    }

}
