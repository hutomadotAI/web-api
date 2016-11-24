package com.hutoma.api.validation;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ITelemetry;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiError;

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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@ValidateParameters
@Provider
@Priority(Priorities.ENTITY_CODER)
//Message encoder or decoder filter/interceptor priority. (happens after auth)
public class QueryFilter extends ParameterFilter implements ContainerRequestFilter {

    private static final String LOGFROM = "queryfilter";

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    public QueryFilter(ILogger logger, Tools tools, JsonSerializer serializer) {
        super(logger, tools, serializer);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        // get the list of things that we need to validate
        final HashSet<APIParameter> checkList = new HashSet<>();
        extractAPIParameters(checkList, this.resourceInfo.getResourceClass());
        extractAPIParameters(checkList, this.resourceInfo.getResourceMethod());

        try {
            // parameters in url path
            final MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();
            // parameters in url query
            final MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();

            // developer ID is always validated
            requestContext.setProperty(APIParameter.DevID.toString(),
                    this.validateAlphaNumPlusDashes(DEVID, requestContext.getHeaderString(DEVID)));

            // extract each parameter as necessary,
            // validate and put the result into a property in the requestcontext
            if (checkList.contains(APIParameter.AIID)) {
                requestContext.setProperty(APIParameter.AIID.toString(),
                        this.validateUuid(AIID, getFirst(pathParameters.get(AIID))));
            }

            if (checkList.contains(APIParameter.AIID_MESH)) {
                requestContext.setProperty(APIParameter.AIID_MESH.toString(),
                        this.validateUuid(AIID_MESH, getFirst(pathParameters.get(AIID_MESH))));
            }

            if (checkList.contains(APIParameter.ChatID)) {
                final String chatId = getFirstOrDefault(queryParameters.get(CHATID), "");
                requestContext.setProperty(APIParameter.ChatID.toString(),
                        this.validateAlphaNumPlusDashes(CHATID,
                                chatId.isEmpty()
                                        ? this.tools.createNewRandomUUID().toString()
                                        : this.validateAlphaNumPlusDashes(CHATID, chatId)));
            }
            if (checkList.contains(APIParameter.EntityName)) {
                requestContext.setProperty(APIParameter.EntityName.toString(),
                        this.validateAlphaNumPlusDashes(ENTITYNAME, getFirst(queryParameters.get(ENTITYNAME))));
            }
            if (checkList.contains(APIParameter.IntentName)) {
                requestContext.setProperty(APIParameter.IntentName.toString(),
                        this.validateAlphaNumPlusDashes(INTENTNAME, getFirst(queryParameters.get(INTENTNAME))));
            }
            if (checkList.contains(APIParameter.ChatQuestion)) {
                requestContext.setProperty(APIParameter.ChatQuestion.toString(),
                        this.validateRequiredSanitized("question", getFirst(queryParameters.get(CHATQUESTION))));
            }
            if (checkList.contains(APIParameter.ChatHistory)) {
                requestContext.setProperty(APIParameter.ChatHistory.toString(),
                        this.validateOptionalSanitized(getFirst(queryParameters.get(CHATHISTORY))));
            }
            if (checkList.contains(APIParameter.AIName)) {
                requestContext.setProperty(APIParameter.AIName.toString(),
                        this.validateAiName(AINAME, getFirst(queryParameters.get(AINAME))));
            }
            if (checkList.contains(APIParameter.AIDescription)) {
                requestContext.setProperty(APIParameter.AIDescription.toString(),
                        this.validateOptionalDescription(AIDESC, getFirst(queryParameters.get(AIDESC))));
            }
            if (checkList.contains(APIParameter.ChatTopic)) {
                requestContext.setProperty(APIParameter.ChatTopic.toString(),
                        this.validateOptionalSanitizeRemoveAt(TOPIC, getFirst(queryParameters.get(TOPIC))));
            }
            if (checkList.contains(APIParameter.Min_P)) {
                requestContext.setProperty(APIParameter.Min_P.toString(),
                        this.validateOptionalFloat(MINP, 0.0f, 1.0f, 0.0f, getFirst(queryParameters.get(MINP))));
            }
            this.logger.logDebug(LOGFROM, "parameter validation passed");


        } catch (ParameterValidationException pve) {
            requestContext.abortWith(ApiError.getBadRequest(pve).getResponse(this.serializer).build());
            this.logger.logDebug(LOGFROM, "parameter validation failed");
            ITelemetry.addTelemetryEvent(this.logger, "ParamValidationFailed (QUERY)");
        }
    }

    /***
     * Extract the param list from the annotated element
     */
    private void extractAPIParameters(final Set<APIParameter> container,
                                      final AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            final ValidateParameters validateParameters = annotatedElement.getAnnotation(ValidateParameters.class);
            if (validateParameters != null) {
                final APIParameter[] allowedAPIParameters = validateParameters.value();
                container.addAll(Arrays.asList(allowedAPIParameters));
            }
        }
    }

}