package com.hutoma.api.validation;

import com.google.gson.JsonParseException;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiFacebookCustomisation;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.facebook.FacebookConnect;
import com.hutoma.api.containers.facebook.FacebookNotification;
import com.hutoma.api.containers.sub.*;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.logic.chat.ChatDefaultHandler;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ContainerRequest;

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
import java.lang.reflect.AnnotatedElement;
import java.util.*;

@ValidatePost
@Provider
@Priority(Priorities.ENTITY_CODER)
//Message encoder or decoder filter/interceptor priority. (happens after auth)
public class PostFilter extends ParameterFilter implements ContainerRequestFilter {

    private static final String LOGFROM = "postfilter";

    private final Config config;

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    PostFilter(final ILogger logger, final Tools tools, final JsonSerializer serializer,
               final Config config) {
        super(logger, tools, serializer);
        this.config = config;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) {

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
                case BotStructure:
                    //fallthrough
                case FacebookConnect:
                    //fallthrough
                case FacebookNotification:
                    //fallthrough
                case FacebookCustomisations:
                    //fallthrough
                case ContextVariables:
                    //fallthrough
                case WebHookReponse:
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
            requestContext.abortWith(getValidationBadRequest(pve).getResponse(this.serializer).build());
            this.logger.logUserWarnEvent(LOGFROM, "ParameterValidation", getDeveloperId(requestContext),
                    LogMap.map("Type", "Post")
                            .put("Parameter", pve.getParameterName())
                            .put("Message", pve.getMessage()));
        } catch (Exception ex) {
            requestContext.abortWith(ApiError.getInternalServerError(ex.getMessage())
                    .getResponse(this.serializer).build());
            this.logger.logUserExceptionEvent(LOGFROM, "ParameterValidation", getDeveloperId(requestContext), ex,
                    LogMap.map("Type", "Post")
                            .put("Parameters", StringUtils.join(
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
                    validateFieldLength(50, AINAME,
                            validateAiName(AINAME, getFirst(form.get(AINAME)))));
        }

        if (checkList.contains(APIParameter.AIID)) {
            request.setProperty(APIParameter.AIID.toString(),
                    validateUuid(AIID, getFirst(form.get(AIID))));
        }

        if (checkList.contains(APIParameter.AIDescription)) {
            request.setProperty(APIParameter.AIDescription.toString(),
                    validateFieldLength(250, AIDESC,
                            filterControlAndCoalesceSpaces(getFirst(form.get(AIDESC)))));
        }
        if (checkList.contains(APIParameter.AiConfidence)) {
            request.setProperty(APIParameter.AiConfidence.toString(),
                    validateFloat(AICONFIDENCE, 0.0f, 1.0f, getFirst(form.get(AICONFIDENCE))));
        }
        if (checkList.contains(APIParameter.Timezone)) {
            request.setProperty(APIParameter.Timezone.toString(),
                    validateTimezoneString(TIMEZONE, getFirst(form.get(TIMEZONE))));
        }
        if (checkList.contains(APIParameter.Locale)) {
            request.setProperty(APIParameter.Locale.toString(),
                    validateLocale(LOCALE, getFirst(form.get(LOCALE))));
        }
        if (checkList.contains(APIParameter.DefaultChatResponses)) {
            // We should receive a Json list
            String jsonList = getFirst(form.get(DEFAULT_CHAT_RESPONSES));
            List<String> list = jsonList.isEmpty()
                    ? Collections.singletonList(ChatDefaultHandler.COMPLETELY_LOST_RESULT)
                    : this.serializer.deserializeListAutoDetect(jsonList);
            request.setProperty(APIParameter.DefaultChatResponses.toString(), list);
        }
        if (checkList.contains(APIParameter.PublishingType)) {
            request.setProperty(APIParameter.PublishingType.toString(),
                    validatePublishingType(getFirst(form.get(PUBLISHING_TYPE))));
        }
    }

    private void processJson(final ContainerRequest request, final HashSet<APIParameter> checkList)
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
                this.validateEntity(entity);
                request.setProperty(APIParameter.EntityJson.toString(), entity);
            }

            // verify an intent object delivered in json
            if (checkList.contains(APIParameter.IntentJson)) {
                // decode
                ApiIntent intent = (ApiIntent) this.serializer.deserialize(request.getEntityStream(), ApiIntent.class);
                this.validateIntent(intent);
                request.setProperty(APIParameter.IntentJson.toString(), intent);
            }

            if (checkList.contains(APIParameter.BotStructure)) {
                BotStructure botStructure = (BotStructure)
                        this.serializer.deserialize(request.getEntityStream(), BotStructure.class);
                this.validateBotStructure(botStructure, UUID.fromString(getDeveloperId(request)));
                request.setProperty(APIParameter.BotStructure.toString(), botStructure);
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
                validateFieldLength(1000, "greeting", facebookCustomisation.getPageGreeting());
                request.setProperty(APIParameter.FacebookCustomisations.toString(), facebookCustomisation);
            }

            if (checkList.contains(APIParameter.ContextVariables)) {
                Map<String, String> variables =
                        this.serializer.deserializeStringMap(request.getEntityStream());
                request.setProperty(APIParameter.ContextVariables.toString(), variables);
            }

            if (checkList.contains(APIParameter.WebHookReponse)) {
                WebHookResponse webHookResponse = (WebHookResponse) this.serializer.deserialize(
                        request.getEntityStream(), WebHookResponse.class);
                request.setProperty(APIParameter.WebHookReponse.toString(), webHookResponse);
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

    /***
     * Verify that a provided intent is valid.
     * @param intent The Intent.
     * @throws ParameterValidationException
     */
    void validateIntent(final ApiIntent intent) throws ParameterValidationException {
        // validate name
        validateIntentName(intent.getIntentName());

        if (intent.getResponses() != null && !intent.getResponses().isEmpty()) {
            // for each response, filter, check against size limit, dedupe, remove empties, check one present
            intent.setResponses(
                    dedupeAndEnsureNonEmptyList(
                            validateFieldLengthsInList(INTENT_RESPONSE_MAX_LENGTH, INTENT_RESPONSES,
                                    filterControlCoalesceSpacesInList(intent.getResponses())), INTENT_RESPONSES));

            // check responses limit
            if (intent.getResponses().size() > config.getMaxIntentResponses()) {
                throw new ParameterValidationException(String.format("number of responses (%d) exceeds limit (%d)",
                        intent.getResponses().size(), config.getMaxIntentResponses()), INTENT_RESPONSES);
            }
        }

        if (intent.getUserSays() != null && !intent.getUserSays().isEmpty()) {
            // for each expression, filter, check against size limit, dedupe, remove empties, check one present
            intent.setUserSays(
                    dedupeAndEnsureNonEmptyList(
                            validateFieldLengthsInList(INTENT_USERSAYS_MAX_LENGTH, INTENT_USERSAYS,
                                    filterControlCoalesceSpacesInList(intent.getUserSays())), INTENT_USERSAYS));
            // check expression limit
            if (intent.getUserSays().size() > config.getMaxIntentUserSays()) {
                throw new ParameterValidationException(String.format("number of expressions (%d) exceeds limit (%d)",
                        intent.getUserSays().size(), config.getMaxIntentUserSays()), INTENT_USERSAYS);
            }
        }

        HashSet<String> labelsInUse = new HashSet<>();
        // for each variable
        if (intent.getVariables() != null) {
            for (IntentVariable variable : intent.getVariables()) {
                // validate the name
                validateFieldLength(250, ENTITYNAME, variable.getEntityName());
                validateIntentEntityName(ENTITYNAME, variable.getEntityName());

                // the list of prompts
                List<String> prompts = validateFieldLengthsInList(INTENT_PROMPT_MAX_LENGTH, INTENT_PROMPTLIST,
                        filterControlCoalesceSpacesInList(variable.getPrompts()));
                if (variable.isRequired()) {
                    prompts = dedupeAndEnsureNonEmptyList(prompts, INTENT_PROMPTLIST);
                }
                variable.setPrompts(prompts);

                // the value
                validateFieldLength(250, INTENT_VAR_VALUE, variable.getValue());
                validateOptionalDescription(INTENT_VAR_VALUE, variable.getValue());

                // the label
                validateFieldLength(250, INTENT_VAR_LABEL, variable.getLabel());

                // get a trimmed label, to use for uniqueness check
                // also validate characters in the process
                String label = validateRequiredLabel(INTENT_VAR_LABEL, variable.getLabel());
                if (!labelsInUse.add(label)) {
                    throw new ParameterValidationException("duplicate intent variable label", INTENT_VAR_LABEL);
                }
            }
        }

        WebHook webHook = intent.getWebHook();
        if (webHook != null) {
            this.checkParameterNotNull("enabled", webHook.isEnabled());
            validateIntentName(webHook.getIntentName());

            if (webHook.isEnabled()) {
                validateFieldLength(2048, "endpoint", webHook.getEndpoint());
                this.checkParameterNotNull("endpoint", webHook.getEndpoint());
                this.checkParameterNotNull(AIID, webHook.getAiid());
            }
        }
    }

    /***
     * Verify that a provided entity is valid.
     * @param entity The Entity.
     * @throws ParameterValidationException
     */
    void validateEntity(final ApiEntity entity) throws ParameterValidationException {
        validateEntityName(ENTITYNAME, entity.getEntityName());
        validateUniqueList(ENTITYVALUE, entity.getEntityValueList());
        if (entity.getEntityValueType() == null) {
            entity.setEntityValueType(EntityValueType.LIST);
        }
        if (entity.getEntityValueType() == EntityValueType.SYS) {
            throw new ParameterValidationException(ENTITY_VALUETYPE, "Cannot create entity with system typed values");
        }
    }

    /***
     * Verify that a provided BotStructure is valid.
     * @param botStructure The BotStructure.
     * @throws ParameterValidationException
     */
    void validateBotStructure(final BotStructure botStructure, final UUID devId) throws ParameterValidationException {

        try {
            checkParameterNotNull("language", botStructure.getLanguage());
            checkParameterNotNull(TIMEZONE, botStructure.getTimezone());
            if (botStructure.getVersion() < 1) {
                throw new ParameterValidationException("Invalid version", "version");
            }

            if (botStructure.getLanguage().equals("en_US")) {
                botStructure.setLanguage("en-US");
            }
            validateLocale(LOCALE, botStructure.getLanguage());
            validateAiName(AINAME, botStructure.getName());
            validateFieldLength(50, AINAME, botStructure.getName());
            validateFieldLength(250, AIDESC,
                    filterControlAndCoalesceSpaces(botStructure.getDescription()));
            validateFieldLength(250, AIDESC, botStructure.getDescription());
            validateTimezoneString(TIMEZONE, botStructure.getTimezone());
            if (botStructure.getEntities() != null) {
                // Prevent any entities from having the system prefix flag
                for (ApiEntity entity : botStructure.getEntities().values()) {
                    this.validateEntity(entity);
                    // Support legacy entities which didn't have a type
                    if (entity.getEntityValueType() == null) {
                        entity.setEntityValueType(EntityValueType.LIST);
                    }
                    if (entity.isSystem() || entity.getEntityValueType() == EntityValueType.SYS) {
                        throw new ParameterValidationException(String.format(
                                "cannot import entity as system: %s",
                                entity.getEntityName()), ENTITYNAME);
                    }
                }
            }
            if (botStructure.getIntents() != null) {
                Set<String> intentNames = new HashSet<>();
                for (ApiIntent intent : botStructure.getIntents()) {
                    if (intentNames.contains(intent.getIntentName())) {
                        throw new ParameterValidationException(String.format(
                                "duplicate intent name: %s",
                                intent.getIntentName()), INTENTNAME);
                    }
                    this.validateIntent(intent);
                    intentNames.add(intent.getIntentName());
                }
            }
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "Error validating bot import structure",
                    devId.toString(), ex);
            if (ex instanceof ParameterValidationException) {
                // rethrow
                throw ex;
            } else {
                // We should have caught all possible parameter failures, but just in case...
                throw new ParameterValidationException("Failed to process the bot structure.", "");
            }
        }
    }
}
