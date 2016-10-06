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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ValidateParameters
@Provider
@Priority(Priorities.ENTITY_CODER) //Message encoder or decoder filter/interceptor priority. (happens after auth)
public class ParameterFilter extends Validate implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private Logger logger;

    @Inject
    private Tools tools;

    @Inject
    JsonSerializer serializer;

    private final String LOGFROM = "validationfilter";

    // query parameter names
    private final String AIID = "aiid";
    private final String DEVID = "_developer_id";
    private final String CHATID = "chatId";
    private final String CHATQUESTION = "q";
    private final String CHATHISTORY = "chat_history";
    private final String AIDESC = "description";
    private final String AINAME = "name";
    private final String TOPIC = "current_topic";
    private final String MINP = "confidence_threshold";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // get the list of things that we need to validate
        HashSet<APIParameter> checkList = new HashSet<>();
        extractAPIParameters(checkList, resourceInfo.getResourceClass());
        extractAPIParameters(checkList, resourceInfo.getResourceMethod());

        try {
            // get maps of parameters
            MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();
            MultivaluedMap<String, String> queryParameters = requestContext.getUriInfo().getQueryParameters();

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
                String chatId = getFirstOrDefault(queryParameters.get(CHATID), "");
                requestContext.setProperty(APIParameter.ChatID.toString(),
                        this.validateAlphaNumPlusDashes(CHATID,
                                chatId.isEmpty()
                                        ? tools.createNewRandomUUID().toString()
                                        : this.validateAlphaNumPlusDashes(CHATID, chatId)));
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
            logger.logDebug(LOGFROM, "parameter validation passed");

        } catch (ParameterValidationException pve) {
            requestContext.abortWith(ApiError.getBadRequest(pve).getResponse(serializer).build());
            logger.logDebug(LOGFROM, "parameter validation failed");
        }
    }

    // static accessors to retrieve validated parameters from the request context
    public static String getDevid(ContainerRequestContext requestContext) {
        return (String)requestContext.getProperty(APIParameter.DevID.toString());
    }

    public static UUID getAiid(ContainerRequestContext requestContext) {
        return (UUID)requestContext.getProperty(APIParameter.AIID.toString());
    }

    public static String getChatID(ContainerRequestContext requestContext) {
        return (String)requestContext.getProperty(APIParameter.ChatID.toString());
    }

    public static String getChatQuestion(ContainerRequestContext requestContext) {
        return (String)requestContext.getProperty(APIParameter.ChatQuestion.toString());
    }

    public static String getChatHistory(ContainerRequestContext requestContext) {
        return (String)requestContext.getProperty(APIParameter.ChatHistory.toString());
    }

    public static String getAiName(ContainerRequestContext requestContext) {
        return (String)requestContext.getProperty(APIParameter.AIName.toString());
    }

    public static String getAiDescription(ContainerRequestContext requestContext) {
        return (String)requestContext.getProperty(APIParameter.AIDescription.toString());
    }

    public static String getTopic(ContainerRequestContext requestContext) {
        return (String)requestContext.getProperty(APIParameter.ChatTopic.toString());
    }

    public static float getMinP(ContainerRequestContext requestContext) {
        return (Float)requestContext.getProperty(APIParameter.Min_P.toString());
    }

    /***
     * Avoids null pointers when the list is null or empty
     * @param list
     * @return empty string or the first string in the list if available
     */
    private String getFirst(List<String> list) {
        return ((null == list) || (list.isEmpty())) ? "" : list.get(0);
    }

    /***
     * Gets the first parameter value, or a default value if there is none
     * @param list
     * @param defaultValue
     * @return
     */
    private String getFirstOrDefault(List<String> list, String defaultValue) {
        return ((null == list) || (list.isEmpty())) ? defaultValue : list.get(0);
    }

    /***
     * Extract the param list from the annotated element
     */
    private void extractAPIParameters(Set<APIParameter> container, AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {
            ValidateParameters validateParameters = annotatedElement.getAnnotation(ValidateParameters.class);
            if (validateParameters != null) {
                APIParameter[] allowedAPIParameters = validateParameters.value();
                container.addAll(Arrays.asList(allowedAPIParameters));
            }
        }
    }

}