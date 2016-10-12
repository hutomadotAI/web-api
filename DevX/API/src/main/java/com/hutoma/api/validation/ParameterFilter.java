package com.hutoma.api.validation;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiError;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

@ValidateParameters
@Provider
@Priority(Priorities.ENTITY_CODER)
//Message encoder or decoder filter/interceptor priority. (happens after auth)
public class ParameterFilter extends Validate implements ContainerRequestFilter {

    private static final String LOGFROM = "validationfilter";
    // query parameter names
    private static final String AIID = "aiid";
    private static final String DEVID = "_developer_id";
    private static final String CHATID = "chatId";
    private static final String CHATQUESTION = "q";
    private static final String CHATHISTORY = "chat_history";
    private static final String AIDESC = "description";
    private static final String AINAME = "name";
    private static final String TOPIC = "current_topic";
    private static final String MINP = "confidence_threshold";
    private static final String ENTITYNAME = "entity_name";
    private static final String INTENTNAME = "intent_name";
    private final Logger logger;
    private final Tools tools;
    private final JsonSerializer serializer;
    @Context
    private ResourceInfo resourceInfo;

    @Inject
    public ParameterFilter(final Logger logger, final Tools tools, final JsonSerializer serializer) {
        this.logger = logger;
        this.tools = tools;
        this.serializer = serializer;
    }

    // static accessors to retrieve validated parameters from the request context
    public static String getDevid(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.DevID.toString());
    }

    public static UUID getAiid(final ContainerRequestContext requestContext) {
        return (UUID) requestContext.getProperty(APIParameter.AIID.toString());
    }

    public static String getChatID(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.ChatID.toString());
    }

    public static String getChatQuestion(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.ChatQuestion.toString());
    }

    public static String getChatHistory(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.ChatHistory.toString());
    }

    public static String getAiName(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.AIName.toString());
    }

    public static String getAiDescription(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.AIDescription.toString());
    }

    public static String getTopic(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.ChatTopic.toString());
    }

    public static float getMinP(final ContainerRequestContext requestContext) {
        return (Float) requestContext.getProperty(APIParameter.Min_P.toString());
    }

    public static String getEntityName(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.EntityName.toString());
    }

    public static String getIntentName(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.IntentName.toString());
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        // get the list of things that we need to validate
        final HashSet<APIParameter> checkList = new HashSet<>();
        extractAPIParameters(checkList, this.resourceInfo.getResourceClass());
        extractAPIParameters(checkList, this.resourceInfo.getResourceMethod());

        try {
            // get maps of parameters
            final MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();
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
                    this.validateAlphaNumPlusDashes(AINAME, getFirst(queryParameters.get(AINAME))));
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

        } catch (final ParameterValidationException pve) {
            requestContext.abortWith(ApiError.getBadRequest(pve).getResponse(this.serializer).build());
            this.logger.logDebug(LOGFROM, "parameter validation failed");
        }
    }

    /***
     * Avoids null pointers when the list is null or empty
     * @param list
     * @return empty string or the first string in the list if available
     */
    private String getFirst(final List<String> list) {
        return ((null == list) || (list.isEmpty())) ? "" : list.get(0);
    }

    /***
     * Gets the first parameter value, or a default value if there is none
     * @param list
     * @param defaultValue
     * @return
     */
    private String getFirstOrDefault(final List<String> list, final String defaultValue) {
        return ((null == list) || (list.isEmpty())) ? defaultValue : list.get(0);
    }

    /***
     * Extract the param list from the annotated element
     */
    private void extractAPIParameters(final Set<APIParameter> container, final AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            final ValidateParameters validateParameters = annotatedElement.getAnnotation(ValidateParameters.class);
            if (validateParameters != null) {
                final APIParameter[] allowedAPIParameters = validateParameters.value();
                container.addAll(Arrays.asList(allowedAPIParameters));
            }
        }
    }

}