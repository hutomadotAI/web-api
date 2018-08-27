package com.hutoma.api.validation;

import com.hutoma.api.common.AnalyticsResponseFormat;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.ApiEntity;
import com.hutoma.api.containers.ApiFacebookCustomisation;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.facebook.FacebookConnect;
import com.hutoma.api.containers.facebook.FacebookNotification;
import com.hutoma.api.containers.sub.AiBot;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.logging.ILogger;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

public class ParameterFilter extends Validate {

    // parameter names
    static final String AICONFIDENCE = "confidence";
    static final String TIMEZONE = "timezone";
    static final String LOCALE = "locale";
    static final String CHATID = "chatId";
    static final String CHATQUESTION = "q";
    static final String AIDESC = "description";
    static final String AINAME = "name";
    static final String MINP = "confidence_threshold";
    static final String ENTITYNAME = "entity_name";
    static final String INTENTNAME = "intent_name";
    static final String ENTITYVALUE = "entity_value";
    static final String INTENT_PROMPTLIST = "intent_prompts";
    static final String INTENT_USERSAYS = "intent_usersays";
    static final String INTENT_RESPONSES = "intent_responses";
    static final String INTENT_VAR_VALUE = "intent_variable_value";
    static final String INTENT_VAR_LABEL = "intent_variable_label";
    static final String TRAINING_SOURCE_TYPE = "source_type";
    static final String FACEBOOK_CONNECT = "facebook_connect";
    static final String ANALYTICS_RESPONSE_FORMAT = "format";
    static final String DEFAULT_CHAT_RESPONSES = "default_chat_responses";
    static final String PUBLISHING_TYPE = "publishing_type";
    static final String BOT_ID_LIST = "bot_list";
    static final String CHAT_HANDOVER_TARGET = "target";
    static final String EXPERIMENT_FEATURE_NAME = "feature";

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

    public static FacebookConnect getFacebookConnect(final ContainerRequestContext requestContext) {
        return (FacebookConnect) requestContext.getProperty(APIParameter.FacebookConnect.toString());
    }

    public static FacebookNotification getFacebookNotification(final ContainerRequestContext requestContext) {
        return (FacebookNotification) requestContext.getProperty(APIParameter.FacebookNotification.toString());
    }

    public static AnalyticsResponseFormat getAnalyticsResponseFormat(final ContainerRequestContext requestContext) {
        return (AnalyticsResponseFormat) requestContext.getProperty(APIParameter.AnalyticsResponseFormat.toString());
    }

    @SuppressWarnings("unchecked")
    public static List<String> getDefaultChatResponses(final ContainerRequestContext requestContext) {
        return (List<String>) requestContext.getProperty(APIParameter.DefaultChatResponses.toString());
    }

    public static ApiFacebookCustomisation getFacebookCustomisations(final ContainerRequestContext requestContext) {
        return (ApiFacebookCustomisation) requestContext.getProperty(APIParameter.FacebookCustomisations.toString());
    }

    public static AiBot.PublishingType getBotPublishingType(final ContainerRequestContext requestContext) {
        return (AiBot.PublishingType) requestContext.getProperty(APIParameter.PublishingType.toString());
    }

    @SuppressWarnings("unchecked")
    public static List<Integer> getBotIdList(final ContainerRequestContext requestContext) {
        return (List<Integer>) requestContext.getProperty(APIParameter.BotIdList.toString());
    }

    public static ChatHandoverTarget getChatHandoverTarget(final ContainerRequestContext requestContext) {
        return (ChatHandoverTarget) requestContext.getProperty(APIParameter.ChatHandoverTarget.toString());
    }

    public static Map<String, String> getContextVariables(final ContainerRequestContext requestContext) {
        return (Map<String, String>) requestContext.getProperty(APIParameter.ContextVariables.toString());
    }

    public static String getExperimentFeatureName(final ContainerRequestContext requestContext) {
        return (String) requestContext.getProperty(APIParameter.ExperimentFeatureName.toString());
    }

    /**
     * Gets the developer id (if any) from the headers in the request context.
     * @param requestContext the request context
     * @return the developer id for the request (if any)
     */
    String getDeveloperId(final ContainerRequestContext requestContext) {
        if (!requestContext.getHeaders().containsKey(DEVID_HEADER_KEY)) {
            return "";
        }
        return requestContext.getHeaders().getFirst(DEVID_HEADER_KEY);
    }
}
