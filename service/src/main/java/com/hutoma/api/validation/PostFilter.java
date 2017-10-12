package com.hutoma.api.validation;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiFacebookCustomisation;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.facebook.FacebookConnect;
import com.hutoma.api.containers.facebook.FacebookNotification;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.logic.ChatLogic;

import org.apache.logging.log4j.util.Strings;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ContainerRequest;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    public PostFilter(ILogger logger, Tools tools, JsonSerializer serializer) {
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
                    //fallthrough
                case EntityJson:
                    //fallthrough
                case AiStatusJson:
                    //fallthrough
                case ServerRegistration:
                    //fallthrough
                case ServerAffinity:
                    //fallthrough
                case FacebookConnect:
                    //fallthrough
                case FacebookNotification:
                    //fallthrough
                case FacebookCustomisations:
                    expectingJson = true;
                    break;

                case AIName:
                    //fallthrough
                case AIDescription:
                    //fallthrough
                case AIID:
                    //fallthrough
                case PublishingType:
                    // fallthrough
                case DefaultChatResponses:
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
                throw new ParameterValidationException("wrong request type", "request context");
            }
            final ContainerRequest request = (ContainerRequest) requestContext;
            // if there is a body to decode
            if (requestContext.getLength() <= 0 || !requestContext.hasEntity()) {
                throw new ParameterValidationException("no form body found", "request context");
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
            this.logger.logUserWarnEvent(LOGFROM, "ParameterValidation", getDeveloperId(requestContext),
                    LogMap.map("Type", "Post")
                            .put("Parameter", pve.getParameterName())
                            .put("Message", pve.getMessage()));
        } catch (Exception ex) {
            requestContext.abortWith(ApiError.getInternalServerError(ex.getMessage())
                    .getResponse(this.serializer).build());
            this.logger.logUserExceptionEvent(LOGFROM, "ParameterValidation", getDeveloperId(requestContext), ex,
                    LogMap.map("Type", "Post")
                            .put("Parameters", Strings.join(
                                    checkList.stream().map(APIParameter::toString).iterator(), ','))
                            .put("RequestLength", requestContext.getLength())
                            .put("Path", requestContext.getUriInfo().getPath()));
        }
    }

    private void processFormVariables(ContainerRequest request, HashSet<APIParameter> checkList)
            throws ParameterValidationException {
        // if the body is of the right type
        if (!MediaTypes.typeEqual(MediaType.APPLICATION_FORM_URLENCODED_TYPE, request.getMediaType())
                && !MediaTypes.typeEqual(MediaType.MULTIPART_FORM_DATA_TYPE, request.getMediaType())) {
            throw new ParameterValidationException("expected form urlencoded type", "content type");
        }
        // buffer it
        request.bufferEntity();
        // get the form as a multivalued map
        final MultivaluedMap<String, String> form = request.readEntity(Form.class).asMap();

        if (checkList.contains(APIParameter.AIName)) {
            request.setProperty(APIParameter.AIName.toString(),
                    this.validateFieldLength(50, AINAME,
                            this.validateAiName(AINAME, getFirst(form.get(AINAME)))));
        }

        if (checkList.contains(APIParameter.AIID)) {
            request.setProperty(APIParameter.AIID.toString(),
                    this.validateUuid(AIID, getFirst(form.get(AIID))));
        }

        if (checkList.contains(APIParameter.AIDescription)) {
            request.setProperty(APIParameter.AIDescription.toString(),
                    this.validateFieldLength(250, AIDESC,
                            this.filterControlAndCoalesceSpaces(getFirst(form.get(AIDESC)))));
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
                    validateLocale(LOCALE, getFirst(form.get(LOCALE))));
        }
        if (checkList.contains(APIParameter.DefaultChatResponses)) {
            // We should receive a Json list
            String jsonList = getFirst(form.get(DEFAULT_CHAT_RESPONSES));
            List<String> list = jsonList.isEmpty()
                    ? Collections.singletonList(ChatLogic.COMPLETELY_LOST_RESULT)
                    : this.serializer.deserializeList(jsonList);
            request.setProperty(APIParameter.DefaultChatResponses.toString(), list);
        }
        if (checkList.contains(APIParameter.PublishingType)) {
            request.setProperty(APIParameter.PublishingType.toString(),
                    validatePublishingType(getFirst(form.get(PUBLISHING_TYPE))));
        }
    }

    private void processJson(ContainerRequest request, HashSet<APIParameter> checkList)
            throws ParameterValidationException {
        // if the body is of the right type
        if (!MediaTypes.typeEqual(MediaType.APPLICATION_JSON_TYPE, request.getMediaType())) {
            throw new ParameterValidationException("expected json encoded body", "content type");
        }
        // buffer it
        request.bufferEntity();

        try {

            if (checkList.contains(APIParameter.EntityJson)) {
                ApiEntity entity = (ApiEntity) this.serializer.deserialize(request.getEntityStream(), ApiEntity.class);
                this.validateEntityName(ENTITYNAME, entity.getEntityName());
                this.validateOptionalObjectValues(ENTITYVALUE, entity.getEntityValueList());
                request.setProperty(APIParameter.EntityJson.toString(), entity);
            }

            // verify an intent object delivered in json
            if (checkList.contains(APIParameter.IntentJson)) {
                // decode
                ApiIntent intent = (ApiIntent) this.serializer.deserialize(request.getEntityStream(), ApiIntent.class);
                // validate name
                this.validateFieldLength(250, INTENTNAME, intent.getIntentName());
                this.validateAlphaNumPlusDashes(INTENTNAME, intent.getIntentName());

                // for each response, filter and check against size limit
                intent.setResponses(
                        this.validateFieldLengthsInList(250, INTENT_RESPONSES,
                                this.filterCoalesceSpacesInList(intent.getResponses())));

                // for each user expression, filter and check against size limit
                intent.setUserSays(
                        this.validateFieldLengthsInList(250, INTENT_USERSAYS,
                                this.filterCoalesceSpacesInList(intent.getUserSays())));

                // for each variable
                if (null != intent.getVariables()) {
                    for (IntentVariable variable : intent.getVariables()) {
                        // validate the name
                        this.validateFieldLength(250, ENTITYNAME, variable.getEntityName());
                        this.validateEntityName(ENTITYNAME, variable.getEntityName());

                        // the list of prompts
                        variable.setPrompts(
                                this.validateFieldLengthsInList(250, INTENT_PROMPTLIST,
                                        this.filterCoalesceSpacesInList(variable.getPrompts())));

                        // the value
                        this.validateFieldLength(250, INTENT_VAR_VALUE, variable.getValue());
                        this.validateOptionalDescription(INTENT_VAR_VALUE, variable.getValue());
                    }
                }

                WebHook webHook = intent.getWebHook();
                if (webHook != null) {
                    this.checkParameterNotNull("enabled", webHook.isEnabled());
                    this.validateFieldLength(250, INTENTNAME, webHook.getIntentName());
                    this.validateAlphaNumPlusDashes(INTENTNAME, webHook.getIntentName());

                    if (webHook.isEnabled()) {
                        this.validateFieldLength(2048, "endpoint", webHook.getEndpoint());
                        this.checkParameterNotNull("endpoint", webHook.getEndpoint());
                        this.checkParameterNotNull(AIID, webHook.getAiid());
                    }
                }
                request.setProperty(APIParameter.IntentJson.toString(), intent);
            }

            if (checkList.contains(APIParameter.AiStatusJson)) {
                AiStatus aiStatus = (AiStatus) this.serializer.deserialize(request.getEntityStream(), AiStatus.class);
                checkParameterNotNull(AIID, aiStatus.getAiid());
                checkParameterNotNull(DEVID, aiStatus.getDevId());
                checkParameterNotNull("training_status", aiStatus.getTrainingStatus());
                checkParameterNotNull("ai_engine", aiStatus.getAiEngine());
                request.setProperty(APIParameter.AiStatusJson.toString(), aiStatus);
            }

            if (checkList.contains(APIParameter.ServerRegistration)) {
                ServerRegistration serverRegistration = (ServerRegistration)
                        this.serializer.deserialize(request.getEntityStream(), ServerRegistration.class);
                checkParameterNotNull(SERVER_TYPE, serverRegistration.getServerType());
                this.validateFieldLength(255, SERVER_URL, serverRegistration.getServerUrl());
                checkParameterNotNull(SERVER_URL, serverRegistration.getServerUrl());
                checkParameterNotNull(AI_LIST, serverRegistration.getAiList());
                for (ServerAiEntry entry : serverRegistration.getAiList()) {
                    checkParameterNotNullInvalid("ai_id", entry.getAiid());
                    checkParameterNotNullInvalid("training_status", entry.getTrainingStatus());
                }
                request.setProperty(APIParameter.ServerRegistration.toString(), serverRegistration);
            }

            if (checkList.contains(APIParameter.ServerAffinity)) {
                ServerAffinity serverAffinity = (ServerAffinity)
                        this.serializer.deserialize(request.getEntityStream(), ServerAffinity.class);
                checkParameterNotNull(SERVER_SESSION_ID, serverAffinity.getServerSessionID());
                checkParameterNotNull(AI_LIST, serverAffinity.getAiList());
                request.setProperty(APIParameter.ServerAffinity.toString(), serverAffinity);
            }

            if (checkList.contains(APIParameter.FacebookConnect)) {
                FacebookConnect facebookConnect = (FacebookConnect)
                        this.serializer.deserialize(request.getEntityStream(), FacebookConnect.class);
                checkParameterNotNull(FACEBOOK_CONNECT, facebookConnect);
                request.setProperty(APIParameter.FacebookConnect.toString(), facebookConnect);
            }

            if (checkList.contains(APIParameter.FacebookNotification)) {
                FacebookNotification facebookNotification = (FacebookNotification)
                        this.serializer.deserialize(request.getEntityStream(), FacebookNotification.class);
                request.setProperty(APIParameter.FacebookNotification.toString(), facebookNotification);
            }

            if (checkList.contains(APIParameter.FacebookCustomisations)) {
                ApiFacebookCustomisation facebookCustomisation = (ApiFacebookCustomisation)
                        this.serializer.deserialize(request.getEntityStream(), ApiFacebookCustomisation.class);
                this.validateFieldLength(1000, "greeting", facebookCustomisation.getPageGreeting());
                request.setProperty(APIParameter.FacebookCustomisations.toString(), facebookCustomisation);
            }

        } catch (JsonParseException jpe) {
            this.logger.logUserErrorEvent(LOGFROM, jpe.getMessage(), getDeveloperId(request), null);
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