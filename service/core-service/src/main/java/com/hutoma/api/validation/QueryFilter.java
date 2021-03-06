package com.hutoma.api.validation;

import com.hutoma.api.common.AnalyticsResponseFormat;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ChatHandoverTarget;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

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
import java.util.Set;

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

            if (checkList.contains(APIParameter.DevID)) {
                requestContext.setProperty(APIParameter.DevID.toString(),
                        validateAlphaNumPlusDashes(DEVID, requestContext.getHeaderString(DEVID)));
            }

            // extract each parameter as necessary,
            // validate and put the result into a property in the requestcontext
            if (checkList.contains(APIParameter.AIID)) {
                requestContext.setProperty(APIParameter.AIID.toString(),
                        validateUuid(AIID, getFirst(pathParameters.get(AIID))));
            }

            if (checkList.contains(APIParameter.ChatID)) {
                final String chatId = getFirstOrDefault(queryParameters.get(CHATID), "");
                requestContext.setProperty(APIParameter.ChatID.toString(),
                        validateAlphaNumPlusDashes(CHATID,
                                chatId.isEmpty()
                                        ? this.tools.createNewRandomUUID().toString()
                                        : validateAlphaNumPlusDashes(CHATID, chatId)));
            }
            if (checkList.contains(APIParameter.EntityName)) {
                requestContext.setProperty(APIParameter.EntityName.toString(),
                        validateFieldLength(250, ENTITYNAME,
                                validateAlphaNumPlusDashes(ENTITYNAME,
                                        getFirst(queryParameters.get(ENTITYNAME)))));
            }
            if (checkList.contains(APIParameter.IntentName)) {
                requestContext.setProperty(APIParameter.IntentName.toString(),
                        validateFieldLength(250, INTENTNAME,
                                validateAlphaNumPlusDashes(INTENTNAME,
                                        getFirst(queryParameters.get(INTENTNAME)))));
            }
            if (checkList.contains(APIParameter.ChatQuestion)) {
                requestContext.setProperty(APIParameter.ChatQuestion.toString(),
                        validateChatQuestion(
                                getFirst(queryParameters.get(CHATQUESTION))));
            }
            if (checkList.contains(APIParameter.AIName)) {
                requestContext.setProperty(APIParameter.AIName.toString(),
                        validateFieldLength(250, AINAME,
                                validateAiName(AINAME, getFirst(queryParameters.get(AINAME)))));
            }
            if (checkList.contains(APIParameter.AIDescription)) {
                requestContext.setProperty(APIParameter.AIDescription.toString(),
                        validateFieldLength(250, AIDESC,
                                validateOptionalDescription(AIDESC, getFirst(queryParameters.get(AIDESC)))));
            }
            if (checkList.contains(APIParameter.Min_P)) {
                requestContext.setProperty(APIParameter.Min_P.toString(),
                        validateOptionalFloat(MINP, 0.0f, 1.0f, 0.0f, getFirst(queryParameters.get(MINP))));
            }
            if (checkList.contains(APIParameter.TrainingSourceType)) {
                requestContext.setProperty(APIParameter.TrainingSourceType.toString(),
                        validateTrainingSourceType(TRAINING_SOURCE_TYPE,
                                getFirst(queryParameters.get(TRAINING_SOURCE_TYPE))));
            }

            if (checkList.contains(APIParameter.AnalyticsResponseFormat)) {
                AnalyticsResponseFormat format = AnalyticsResponseFormat
                        .forName(getFirst(queryParameters.get(ANALYTICS_RESPONSE_FORMAT)));
                if (format == null) {
                    throw new ParameterValidationException("Unsupported format", ANALYTICS_RESPONSE_FORMAT);
                }
                requestContext.setProperty(APIParameter.AnalyticsResponseFormat.toString(), format);
            }

            if (checkList.contains(APIParameter.BotIdList)) {
                requestContext.setProperty(APIParameter.BotIdList.toString(),
                        validateIntegerList(APIParameter.BotIdList.toString(), queryParameters.get(BOT_ID_LIST)));
            }

            if (checkList.contains(APIParameter.ChatHandoverTarget)) {
                try {
                    requestContext.setProperty(APIParameter.ChatHandoverTarget.toString(),
                            ChatHandoverTarget.fromString(getFirst(queryParameters.get(CHAT_HANDOVER_TARGET))));
                } catch (IllegalArgumentException ex) {
                    throw new ParameterValidationException("Unsupported handover target", CHAT_HANDOVER_TARGET);
                }
            }

            if (checkList.contains(APIParameter.ExperimentFeatureName)) {
                requestContext.setProperty(APIParameter.ExperimentFeatureName.toString(),
                        validateFieldLength(50, EXPERIMENT_FEATURE_NAME,
                                validateAlphaNumPlusDashes(
                                        EXPERIMENT_FEATURE_NAME,
                                        getFirst(queryParameters.get(EXPERIMENT_FEATURE_NAME)))));
            }

            this.logger.logDebug(LOGFROM, "parameter validation passed");

        } catch (ParameterValidationException pve) {
            requestContext.abortWith(Validate.getValidationBadRequest(pve).getResponse(this.serializer).build());
            this.logger.logUserWarnEvent(LOGFROM, "ParameterValidation", getDeveloperId(requestContext),
                    LogMap.map("Type", "Query")
                            .put("Parameter", pve.getParameterName())
                            .put("Message", pve.getMessage()));
        }
    }

    /***
     * Check length and clean up content of chat question
     * @param chatQuestion
     * @return cleaned up chat question
     * @throws ParameterValidationException if question is out of spec
     */
    public String validateChatQuestion(String chatQuestion) throws ParameterValidationException {
        return validateFieldLength(1024, "question",
                validateRequiredSanitized("question", chatQuestion));
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
