package com.hutoma.api.validation;

import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerRegistration;

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
    protected static final String AIDESC = "description";
    protected static final String AINAME = "name";
    protected static final String MINP = "confidence_threshold";
    protected static final String ENTITYNAME = "entity_name";
    protected static final String INTENTNAME = "intent_name";
    protected static final String ENTITYVALUE = "entity_value";
    protected static final String INTENT_PROMPTLIST = "intent_prompts";
    protected static final String INTENT_USERSAYS = "intent_usersays";
    protected static final String INTENT_RESPONSES = "intent_responses";
    protected static final String INTENT_VAR_VALUE = "intent_variable_value";
    protected static final String TRAINING_SOURCE_TYPE = "source_type";
    protected static final String SERVER_TYPE = "server_type";
    protected static final String AI_LIST = "ai_list";
    protected static final String SERVER_SESSION_ID = "server_session_id";
    protected static final String SERVER_URL = "server_url";

    private static final String DEVID_HEADER_KEY = "_developer_id";

    protected final ILogger logger;
    protected final Tools tools;
    protected final JsonSerializer serializer;

    @Inject
    public ParameterFilter(final ILogger logger, final Tools tools, final JsonSerializer serializer) {
        this.logger = logger;
        this.tools = tools;
        this.serializer = serializer;
    }

    // static accessors to retrieve validated parameters from the request context
    public static UUID getDevid(final ContainerRequestContext requestContext) {
        final String str = (String) requestContext.getProperty(APIParameter.DevID.toString());
        UUID uuid = UUID.fromString(str);
        return uuid;
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

    public static String getAiName(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.AIName.toString());
    }

    public static String getAiDescription(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.AIDescription.toString());
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

    public static ApiIntent getIntent(final ContainerRequestContext requestContext) {
        return (ApiIntent) requestContext.getProperty(APIParameter.IntentJson.toString());
    }

    public static Float getAiConfidence(final ContainerRequestContext requestContext) {
        return (Float) requestContext.getProperty(APIParameter.AiConfidence.toString());
    }

    public static AiStatus getAiStatus(final ContainerRequestContext requestContext) {
        return (AiStatus) requestContext.getProperty(APIParameter.AiStatusJson.toString());
    }

    public static ServerRegistration getServerRegistration(final ContainerRequestContext requestContext) {
        return (ServerRegistration) requestContext.getProperty(APIParameter.ServerRegistration.toString());
    }

    public static ServerAffinity getServerAffinity(final ContainerRequestContext requestContext) {
        return (ServerAffinity) requestContext.getProperty(APIParameter.ServerAffinity.toString());
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

    /**
     * Gets the developer id (if any) from the headers in the request context.
     * @param requestContext the request context
     * @return the developer id for the request (if any)
     */
    protected String getDeveloperId(final ContainerRequestContext requestContext) {
        if (!requestContext.getHeaders().containsKey(DEVID_HEADER_KEY)) {
            return "";
        }
        return requestContext.getHeaders().getFirst(DEVID_HEADER_KEY);
    }
}