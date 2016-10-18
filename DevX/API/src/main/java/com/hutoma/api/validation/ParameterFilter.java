package com.hutoma.api.validation;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Logger;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiEntity;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

public class ParameterFilter extends Validate {

    // parameter names
    protected static final String AIID = "aiid";
    protected static final String DEVID = "_developer_id";
    protected static final String AICONFIDENCE = "confidence";
    protected static final String TIMEZONE = "timezone";
    protected static final String LOCALE = "locale";
    protected static final String CHATID = "chatId";
    protected static final String CHATQUESTION = "q";
    protected static final String CHATHISTORY = "chat_history";
    protected static final String AIDESC = "description";
    protected static final String AINAME = "name";
    protected static final String TOPIC = "current_topic";
    protected static final String MINP = "confidence_threshold";
    protected static final String ENTITYNAME = "entity_name";
    protected static final String INTENTNAME = "intent_name";
    protected static final String ENTITYVALUE = "entity_value";

    protected final Logger logger;
    protected final Tools tools;
    protected final JsonSerializer serializer;

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

    public static String getTimezone(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.Timezone.toString());
    }

    public static Locale getLocale(final ContainerRequestContext requestContext) {
        return (Locale) requestContext.getProperty(APIParameter.Locale.toString());
    }

    public static ApiEntity getEntity(final ContainerRequestContext requestContext) {
        return (ApiEntity) requestContext.getProperty(APIParameter.EntityJson.toString());
    }

    public static Float getAiConfidence(final ContainerRequestContext requestContext) {
        return (Float) requestContext.getProperty(APIParameter.AiConfidence.toString());
    }

    /***
     * Avoids null pointers when the list is null or empty
     * @param list
     * @return empty string or the first string in the list if available
     */
    protected String getFirst(final List<String> list) {
        return ((null == list) || (list.isEmpty())) ? "" : list.get(0);
    }

    /***
     * Gets the first parameter value, or a default value if there is none
     * @param list
     * @param defaultValue
     * @return
     */
    protected String getFirstOrDefault(final List<String> list, final String defaultValue) {
        return ((null == list) || (list.isEmpty())) ? defaultValue : list.get(0);
    }

}