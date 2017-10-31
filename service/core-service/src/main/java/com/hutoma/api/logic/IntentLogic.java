package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseIntegrityViolationException;
import com.hutoma.api.containers.ApiAi;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.WebHook;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Created by David MG on 05/10/2016.
 */
public class IntentLogic {

    private static final String LOGFROM = "intentlogic";
    private final Config config;
    private final ILogger logger;
    private final DatabaseEntitiesIntents databaseEntitiesIntents;
    private final DatabaseAI databaseAi;
    private final TrainingLogic trainingLogic;
    private final JsonSerializer jsonSerializer;

    @Inject
    public IntentLogic(final Config config, final ILogger logger, final DatabaseEntitiesIntents databaseEntitiesIntents,
                       final DatabaseAI databaseAi, final TrainingLogic trainingLogic,
                       final JsonSerializer jsonSerializer) {
        this.config = config;
        this.logger = logger;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.databaseAi = databaseAi;
        this.trainingLogic = trainingLogic;
        this.jsonSerializer = jsonSerializer;
    }

    public ApiResult getIntents(final UUID devid, final UUID aiid) {
        final String devidString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            final List<String> intentList = this.databaseEntitiesIntents.getIntents(devid, aiid);
            if (intentList.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "GetIntents", devidString, logMap.put("Num Intents", "0"));
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetIntents", devidString,
                    logMap.put("Num Intents", intentList.size()));
            return new ApiIntentList(aiid, intentList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetIntents", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getIntent(final UUID devid, final UUID aiid, final String intentName) {
        final String devidString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid).put("IntentName", intentName);
            boolean aiidValid = this.databaseAi.checkAIBelongsToDevId(devid, aiid);
            if (!aiidValid) {
                return ApiError.getBadRequest("AI not found for this Dev ID");
            }

            ApiIntent intent = this.databaseEntitiesIntents.getIntent(aiid, intentName);
            if (null == intent) {
                this.logger.logUserTraceEvent(LOGFROM, "GetIntent - not found", devidString, logMap);
                return ApiError.getNotFound();
            }
            WebHook webHook = this.databaseEntitiesIntents.getWebHook(aiid, intentName);
            intent.setWebHook(webHook);

            this.logger.logUserTraceEvent(LOGFROM, "GetIntent", devidString, logMap);

            return intent.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetIntent", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult writeIntent(final UUID devid, UUID aiid, ApiIntent intent) {
        String devidString = devid.toString();
        LogMap logMap = LogMap.map("AIID", aiid).put("IntentName", intent.getIntentName());
        try {
            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.jsonSerializer);
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - Bot is RO", devidString, logMap);
                return ApiError.getBadRequest(AILogic.BOT_RO_MESSAGE);
            }

            // Check if there are any variables with duplicate or empty labels
            Set<String> usedLabels = new HashSet<>();
            List<String> duplicateLabels = new ArrayList<>();
            List<IntentVariable> variables = intent.getVariables();
            if (variables != null) {
                Optional<IntentVariable> firstEmptyLabelVar = variables.stream().filter(
                        x -> x.getLabel().isEmpty()).findFirst();
                if (firstEmptyLabelVar.isPresent()) {
                    this.logger.logUserErrorEvent(LOGFROM, "Unlabeled variable", devidString,
                            LogMap.map("Variable", firstEmptyLabelVar.get().getEntityName()).put("AIID", aiid)
                                    .put("IntentName", intent.getIntentName()));
                    return ApiError.getBadRequest(
                            String.format("Unlabeled variable: %s", firstEmptyLabelVar.get().getEntityName()));
                }
                variables.forEach(x -> {
                    if (usedLabels.contains(x.getLabel())) {
                        duplicateLabels.add(x.getLabel());
                    } else {
                        usedLabels.add(x.getLabel());
                    }
                });
            }
            if (!duplicateLabels.isEmpty()) {
                String dupLabelsString = String.join(", ", duplicateLabels);
                this.logger.logUserErrorEvent(LOGFROM, "Duplicate labels", devidString,
                        LogMap.map("DupLabels", dupLabelsString).put("AIID", aiid)
                                .put("IntentName", intent.getIntentName()));
                return ApiError.getBadRequest(
                        String.format("Duplicate label%s: %s", dupLabelsString.isEmpty() ? "" : "s", dupLabelsString));
            }
            this.databaseEntitiesIntents.writeIntent(devid, aiid, intent.getIntentName(), intent);
            WebHook webHook = intent.getWebHook();
            if (webHook != null) {
                if (this.databaseEntitiesIntents.getWebHook(aiid, intent.getIntentName()) != null) {
                    this.databaseEntitiesIntents.updateWebHook(aiid, intent.getIntentName(), webHook.getEndpoint(),
                            webHook.isEnabled());
                    this.logger.logUserTraceEvent(LOGFROM, "UpdateWebHook", devidString, logMap);
                } else {
                    this.databaseEntitiesIntents.createWebHook(aiid, webHook.getIntentName(), webHook.getEndpoint(),
                            webHook.isEnabled());
                    this.logger.logUserTraceEvent(LOGFROM, "WriteWebHook", devidString, logMap);
                }
            }
            this.trainingLogic.stopTraining(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent", devidString, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (DatabaseEntitiesIntents.DatabaseEntityException dmee) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - entity duplicate or non existent", devidString,
                    logMap.put("Message", dmee.getMessage()));
            return ApiError.getBadRequest("Duplicate or missing entity_name");
        } catch (DatabaseIntegrityViolationException dive) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - attempt to rename existing name",
                    devidString, logMap);
            return ApiError.getBadRequest("Intent name already in use");
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WriteIntent", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteIntent(final UUID devid, UUID aiid, String intentName) {
        String devidString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid).put("IntentName", intentName);
            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.jsonSerializer);
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteIntent - Bot is RO", devidString, logMap);
                return ApiError.getBadRequest(AILogic.BOT_RO_MESSAGE);
            }
            if (!this.databaseEntitiesIntents.deleteIntent(devid, aiid, intentName)) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteIntent - not found", devidString, logMap);
                return ApiError.getNotFound();
            }
            if (this.databaseEntitiesIntents.getWebHook(aiid, intentName) != null) {
                this.databaseEntitiesIntents.deleteWebHook(aiid, intentName);
            }
            this.trainingLogic.stopTraining(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "DeleteIntent", devidString, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteIntent", devidString, e);
            return ApiError.getInternalServerError();
        }
    }
}


