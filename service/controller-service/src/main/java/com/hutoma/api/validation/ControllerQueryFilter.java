package com.hutoma.api.validation;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.LogMap;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@ValidateControllerParameters
@Provider
@Priority(Priorities.ENTITY_CODER)
public class ControllerQueryFilter extends ControllerParameterFilter implements ContainerRequestFilter {

    private static final String LOGFROM = "controllerqueryfilter";

    private static final String REQUEST_FOR = "for";
    private static final String SERVER_TYPE = "serverType";
    private static final String SERVER_LANGUAGE = "serverLanguage";
    private static final String SERVER_VERSION = "serverVersion";

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    public ControllerQueryFilter(final AiServiceStatusLogger logger, final Tools tools,
                                 final JsonSerializer serializer) {
        super(logger, tools, serializer);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        // get the list of things that we need to validate
        final HashSet<ControllerParameter> checkList = new HashSet<>();
        extractParameters(checkList, this.resourceInfo.getResourceClass());
        extractParameters(checkList, this.resourceInfo.getResourceMethod());

        try {
            // parameters in url path
            final MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();
            // parameters in url query
            final MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();

            if (checkList.contains(ControllerParameter.AIID)) {
                requestContext.setProperty(ControllerParameter.AIID.toString(),
                        validateUuid(AIID, getFirst(pathParameters.get(AIID))));
            }

            if (checkList.contains(ControllerParameter.RequestFor)) {
                requestContext.setProperty(ControllerParameter.RequestFor.toString(),
                        validateRequestFor(getFirst(queryParameters.get(REQUEST_FOR))));
            }

            if (checkList.contains(ControllerParameter.ServerType)) {
                requestContext.setProperty(ControllerParameter.ServerType.toString(),
                        validateServerType(getFirst(queryParameters.get(SERVER_TYPE))));
            }

            if (checkList.contains(ControllerParameter.ServerLanguage)) {
                requestContext.setProperty(ControllerParameter.ServerLanguage.toString(),
                        validateLanguage(getFirst(queryParameters.get(SERVER_LANGUAGE))));
            }

            if (checkList.contains(ControllerParameter.ServerVersion)) {
                requestContext.setProperty(ControllerParameter.ServerVersion.toString(),
                        validateServerVersion(getFirst(queryParameters.get(SERVER_VERSION))));
            }

            this.logger.logDebug(LOGFROM, "parameter validation passed");

        } catch (ParameterValidationException pve) {
            requestContext.abortWith(getValidationBadRequest(pve).getResponse(this.serializer).build());
            this.logger.logWarning(LOGFROM, "ControllerParameterValidation",
                    LogMap.map("Type", "Query")
                            .put("Parameter", pve.getParameterName())
                            .put("Message", pve.getMessage()));
        }
    }

    /***
     * Extract the param list from the annotated element
     */
    private void extractParameters(final Set<ControllerParameter> container,
                                      final AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            final ValidateControllerParameters validateParameters =
                    annotatedElement.getAnnotation(ValidateControllerParameters.class);
            if (validateParameters != null) {
                final ControllerParameter[] allowedAPIParameters = validateParameters.value();
                container.addAll(Arrays.asList(allowedAPIParameters));
            }
        }
    }

    private static RequestFor validateRequestFor(final String requestForString) throws ParameterValidationException {
        Optional<RequestFor> value = Arrays.stream(RequestFor.values())
                .filter(x -> x.name().equalsIgnoreCase(requestForString)).findFirst();
        if (value.isPresent()) {
            return value.get();
        }
        throw new ParameterValidationException("Unknown request for", REQUEST_FOR);
    }

    private static BackendServerType validateServerType(final String serverType) throws ParameterValidationException {
        Optional<BackendServerType> value = Arrays.stream(BackendServerType.values())
                .filter(x -> x.value().equalsIgnoreCase(serverType)).findFirst();
        if (value.isPresent()) {
            return value.get();
        }
        throw new ParameterValidationException("Unknown server type", SERVER_TYPE);
    }

    private static SupportedLanguage validateLanguage(final String language) throws ParameterValidationException {
        if (Tools.isEmpty(language)) {
            return SupportedLanguage.EN;
        }
        Optional<SupportedLanguage> value = Arrays.stream(SupportedLanguage.values())
                .filter(x -> x.toString().equalsIgnoreCase(language)).findFirst();
        if (value.isPresent()) {
            return value.get();
        }
        throw new ParameterValidationException("Unsupported language", SERVER_LANGUAGE);
    }

    private static String validateServerVersion(final String version) throws ParameterValidationException {
        if (Tools.isEmpty(version)) {
            return ServiceIdentity.DEFAULT_VERSION;
        }
        if (version.length() > 10) {
            throw new ParameterValidationException("Unsupported length for server version", SERVER_VERSION);
        }
        return version;
    }
}
