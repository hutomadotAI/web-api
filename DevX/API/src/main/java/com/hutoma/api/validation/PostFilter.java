package com.hutoma.api.validation;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.ITelemetry;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.IntentVariable;

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
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@ValidatePost
@Provider
@Priority(Priorities.ENTITY_CODER)
//Message encoder or decoder filter/interceptor priority. (happens after auth)
public class PostFilter extends ParameterFilter implements ContainerRequestFilter {

    private static final String LOGFROM = "postfilter";

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    public PostFilter(Logger logger, Tools tools, JsonSerializer serializer) {
        super(logger, tools, serializer);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        // get the list of things that we need to validate
        final HashSet<APIParameter> checkList = new HashSet<>();
        extractAPIParameters(checkList, this.resourceInfo.getResourceClass());
        extractAPIParameters(checkList, this.resourceInfo.getResourceMethod());

        // what are we looking for?
        boolean expectingJson = false;
        boolean expectingForm = false;

        // which parameters are of which body type?
        for (APIParameter param : checkList) {
            switch (param) {
                case IntentJson:
                case EntityJson:
                    expectingJson = true;
                    break;
                case AIName:
                case AIDescription:
                    expectingForm = true;
                    break;
                default:
                    break;
            }
        }

        // no post data expected, so exit here
        if ((!expectingJson) && (!expectingForm)) {
            this.logger.logDebug(LOGFROM, "nothing to validate");
            return;
        }

        // in case we inadvertently try to validate data from different kinds of body
        if (expectingForm && expectingJson) {
            requestContext.abortWith(ApiError.getInternalServerError().getResponse(this.serializer).build());
            this.logger.logError(LOGFROM, "we are expecting form and json data at the same time");
            return;
        }

        try {

            // do we have a valid post body?
            if (!(requestContext instanceof ContainerRequest)) {
                throw new ParameterValidationException("wrong request type");
            }
            final ContainerRequest request = (ContainerRequest) requestContext;
            // if there is a body to decode
            if (!requestContext.hasEntity()) {
                throw new ParameterValidationException("no form body found");
            }

            // of which type?
            if (expectingForm) {
                processFormVariables(request, checkList);
            } else {
                processJson(request, checkList);
            }
            this.logger.logDebug(LOGFROM, "post data validation passed");

        } catch (ParameterValidationException pve) {
            requestContext.abortWith(ApiError.getBadRequest(pve).getResponse(this.serializer).build());
            this.logger.logDebug(LOGFROM, "parameter validation failed");
            ITelemetry.addTelemetryEvent(this.logger, "ParamValidationFailed (POST)");
        }
    }

    private void processFormVariables(ContainerRequest request, HashSet<APIParameter> checkList)
            throws ParameterValidationException {
        // if the body is of the right type
        if (!MediaTypes.typeEqual(MediaType.APPLICATION_FORM_URLENCODED_TYPE, request.getMediaType())
                && !MediaTypes.typeEqual(MediaType.MULTIPART_FORM_DATA_TYPE, request.getMediaType())) {
            throw new ParameterValidationException("expected form urlencoded type");
        }
        // buffer it
        request.bufferEntity();
        // get the form as a multivalued map
        final MultivaluedMap<String, String> form = request.readEntity(Form.class).asMap();

        if (checkList.contains(APIParameter.AIName)) {
            request.setProperty(APIParameter.AIName.toString(),
                    this.validateAiName(AINAME, getFirst(form.get(AINAME))));
        }
        if (checkList.contains(APIParameter.AIDescription)) {
            request.setProperty(APIParameter.AIDescription.toString(),
                    this.validateOptionalDescription(AIDESC, getFirst(form.get(AIDESC))));
        }
        if (checkList.contains(APIParameter.AiConfidence)) {
            request.setProperty(APIParameter.AiConfidence.toString(),
                    this.validateFloat(AICONFIDENCE, 0.0f, 1.0f, getFirst(form.get(AICONFIDENCE))));
        }
        if (checkList.contains(APIParameter.Timezone)) {
            request.setProperty(APIParameter.Timezone.toString(),
                    this.validateTimezoneString(TIMEZONE, getFirst(form.get(TIMEZONE))));
        }
        if (checkList.contains(APIParameter.Locale)) {
            request.setProperty(APIParameter.Locale.toString(),
                    this.validateLocale(LOCALE, getFirst(form.get(LOCALE))));
        }
    }

    private void processJson(ContainerRequest request, HashSet<APIParameter> checkList)
            throws ParameterValidationException {
        // if the body is of the right type
        if (!MediaTypes.typeEqual(MediaType.APPLICATION_JSON_TYPE, request.getMediaType())) {
            throw new ParameterValidationException("expected json encoded body");
        }
        // buffer it
        request.bufferEntity();

        try {

            if (checkList.contains(APIParameter.EntityJson)) {
                ApiEntity entity = (ApiEntity) this.serializer.deserialize(request.getEntityStream(), ApiEntity.class);
                this.validateAlphaNumPlusDashes(ENTITYNAME, entity.getEntityName());
                this.validateOptionalObjectValues(ENTITYVALUE, entity.getEntityValueList());
                request.setProperty(APIParameter.EntityJson.toString(), entity);
            }

            // verify an intent object delivered in json
            if (checkList.contains(APIParameter.IntentJson)) {
                // decode
                ApiIntent intent = (ApiIntent) this.serializer.deserialize(request.getEntityStream(), ApiIntent.class);
                // validate name
                this.validateAlphaNumPlusDashes(INTENTNAME, intent.getIntentName());
                // for each variable
                if (null != intent.getVariables()) {
                    for (IntentVariable variable : intent.getVariables()) {
                        // validate the name
                        this.validateAlphaNumPlusDashes(ENTITYNAME, variable.getEntityName());
                        // the list of prompts
                        variable.setPrompts(this.validateOptionalDescriptionList(INTENT_PROMPTLIST,
                                variable.getPrompts()));
                        // the value
                        this.validateOptionalDescription(INTENT_VAR_VALUE, variable.getValue());
                    }
                    // the list of user_says strings
                    intent.setUserSays(this.validateOptionalDescriptionList(INTENT_USERSAYS, intent.getUserSays()));
                    // the list of responses
                    intent.setResponses(this.validateOptionalDescriptionList(INTENT_RESPONSES, intent.getResponses()));
                }
                request.setProperty(APIParameter.IntentJson.toString(), intent);
            }
        } catch (JsonParseException jpe) {
            throw new ParameterValidationException("error in json format");
        }
    }

    /***
     * Extract the param list from the annotated element
     */
    private void extractAPIParameters(final Set<APIParameter> container,
                                      final AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            final ValidatePost validateParameters = annotatedElement.getAnnotation(ValidatePost.class);
            if (validateParameters != null) {
                final APIParameter[] allowedAPIParameters = validateParameters.value();
                container.addAll(Arrays.asList(allowedAPIParameters));
            }
        }
    }
}