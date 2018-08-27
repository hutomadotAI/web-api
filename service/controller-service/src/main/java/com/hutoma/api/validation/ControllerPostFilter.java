package com.hutoma.api.validation;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.ServerEndpointRequestMulti;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.LogMap;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ContainerRequest;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@ValidateControllerPost
@Provider
@Priority(Priorities.ENTITY_CODER)
public class ControllerPostFilter extends ControllerParameterFilter implements ContainerRequestFilter {

    private static final String LOGFROM = "controllerpostfilter";

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    public ControllerPostFilter(final AiServiceStatusLogger logger, final Tools tools,
                                final JsonSerializer serializer) {
        super(logger, tools, serializer);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        // get the list of things that we need to validate
        final HashSet<ControllerParameter> checkList = new HashSet<>();
        extractControllerParameters(checkList, this.resourceInfo.getResourceClass());
        extractControllerParameters(checkList, this.resourceInfo.getResourceMethod());

        try {

            // do we have a valid post body?
            if (!(requestContext instanceof ContainerRequest)) {
                throw new ParameterValidationException("wrong request type", "request context");
            }
            final ContainerRequest request = (ContainerRequest) requestContext;
            // if there is a body to decode
            if (requestContext.getLength() <= 0 || !requestContext.hasEntity()) {
                throw new ParameterValidationException("no form body found", "request context");
            }

            processJson(request, checkList);

            this.logger.logDebug(LOGFROM, "post data validation passed");

        } catch (ParameterValidationException pve) {
            requestContext.abortWith(getValidationBadRequest(pve).getResponse(this.serializer).build());
            this.logger.logWarning(LOGFROM, "ParameterValidation",
                    LogMap.map("Type", "Post")
                            .put("Parameter", pve.getParameterName())
                            .put("Message", pve.getMessage()));
        } catch (Exception ex) {
            requestContext.abortWith(ApiError.getInternalServerError(ex.getMessage())
                    .getResponse(this.serializer).build());
            this.logger.logException(LOGFROM, ex,
                    LogMap.map("Type", "Post")
                            .put("Parameters", StringUtils.join(
                                    checkList.stream().map(ControllerParameter::toString).iterator(), ','))
                            .put("RequestLength", requestContext.getLength())
                            .put("Path", requestContext.getUriInfo().getPath()));
        }
    }

    private void processJson(ContainerRequest request, HashSet<ControllerParameter> checkList)
            throws ParameterValidationException {
        // if the body is of the right type
        if (!MediaTypes.typeEqual(MediaType.APPLICATION_JSON_TYPE, request.getMediaType())) {
            throw new ParameterValidationException("expected json encoded body", "content type");
        }
        // buffer it
        request.bufferEntity();

        try {

            if (checkList.contains(ControllerParameter.AiStatusJson)) {
                AiStatus aiStatus = (AiStatus) this.serializer.deserialize(request.getEntityStream(), AiStatus.class);
                checkParameterNotNull(AIID, aiStatus.getAiid());
                checkParameterNotNull(DEVID, aiStatus.getDevId());
                checkParameterNotNull("training_status", aiStatus.getTrainingStatus());
                checkParameterNotNull("ai_engine", aiStatus.getAiEngine());
                request.setProperty(ControllerParameter.AiStatusJson.toString(), aiStatus);
            }

            if (checkList.contains(ControllerParameter.ServerRegistration)) {
                ServerRegistration serverRegistration = (ServerRegistration)
                        this.serializer.deserialize(request.getEntityStream(), ServerRegistration.class);
                checkParameterNotNull(SERVER_TYPE, serverRegistration.getServerType());
                validateFieldLength(255, SERVER_URL, serverRegistration.getServerUrl());
                checkParameterNotNull(SERVER_URL, serverRegistration.getServerUrl());
                checkParameterNotNull(AI_LIST, serverRegistration.getAiList());
                for (ServerAiEntry entry : serverRegistration.getAiList()) {
                    checkParameterNotNullInvalid("ai_id", entry.getAiid());
                    checkParameterNotNullInvalid("training_status", entry.getTrainingStatus());
                }
                request.setProperty(ControllerParameter.ServerRegistration.toString(), serverRegistration);
            }

            if (checkList.contains(ControllerParameter.ServerAffinity)) {
                ServerAffinity serverAffinity = (ServerAffinity)
                        this.serializer.deserialize(request.getEntityStream(), ServerAffinity.class);
                checkParameterNotNull(SERVER_SESSION_ID, serverAffinity.getServerSessionID());
                checkParameterNotNull(AI_LIST, serverAffinity.getAiList());
                request.setProperty(ControllerParameter.ServerAffinity.toString(), serverAffinity);
            }

            if (checkList.contains(ControllerParameter.ServerEndpointMulti)) {
                ServerEndpointRequestMulti serverEndpointRequestMulti = (ServerEndpointRequestMulti)
                        this.serializer.deserialize(request.getEntityStream(), ServerEndpointRequestMulti.class);
                checkParameterNotNull(ENDPOINT_REQUEST_LIST, serverEndpointRequestMulti.getEndpointRequests());
                request.setProperty(ControllerParameter.ServerEndpointMulti.toString(), serverEndpointRequestMulti);
            }

        } catch (JsonParseException jpe) {
            this.logger.logError(LOGFROM, jpe.getMessage());
            throw new ParameterValidationException("error in json format", "request body");
        }
    }

    private void checkParameterNotNull(final String paramName, final Object obj)
            throws ParameterValidationException {
        if (obj == null) {
            throw new ParameterValidationException("parameter is null", paramName);
        }
    }

    /***
     * For internal fields that are json deserialized, if the data does not parse
     * then the result is a null
     * @param paramName
     * @param obj
     * @throws ParameterValidationException
     */
    private void checkParameterNotNullInvalid(final String paramName, final Object obj)
            throws ParameterValidationException {
        if (obj == null) {
            throw new ParameterValidationException("parameter is null or invalid", paramName);
        }
    }

    /***
     * Extract the param list from the annotated element
     */
    private void extractControllerParameters(final Set<ControllerParameter> container,
                                      final AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            final ValidateControllerPost validateParameters =
                    annotatedElement.getAnnotation(ValidateControllerPost.class);
            if (validateParameters != null) {
                final ControllerParameter[] allowedAPIParameters = validateParameters.value();
                container.addAll(Arrays.asList(allowedAPIParameters));
            }
        }
    }

}
