package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.DatabaseAI;
import com.hutoma.api.connectors.db.DatabaseEntitiesIntents;
import com.hutoma.api.connectors.db.DatabaseIntegrityViolationException;
import com.hutoma.api.connectors.db.DatabaseTransaction;
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
import javax.inject.Provider;

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
    private final Provider<DatabaseTransaction> databaseTransactionProvider;

    @Inject
    IntentLogic(final Config config, final ILogger logger, final DatabaseEntitiesIntents databaseEntitiesIntents,
                       final DatabaseAI databaseAi, final TrainingLogic trainingLogic,
                       final JsonSerializer jsonSerializer,
                       final Provider<DatabaseTransaction> transactionProvider) {
        this.config = config;
        this.logger = logger;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.databaseAi = databaseAi;
        this.trainingLogic = trainingLogic;
        this.jsonSerializer = jsonSerializer;
        this.databaseTransactionProvider = transactionProvider;
    }

    public ApiResult getIntents(final UUID devid, final UUID aiid) {
        final String devidString = devid.toString();
        try {
            LogMap logMap = LogMap.map("AIID", aiid);
            boolean aiidValid = this.databaseAi.checkAIBelongsToDevId(devid, aiid);
            if (!aiidValid) {
                this.logger.logUserTraceEvent(LOGFROM, "GetIntents - AI not found for devId", devidString,
                        logMap);
                return ApiError.getNotFound("AI not found for this Dev ID");
            }
            final ApiIntentList intentDetailsList = this.databaseEntitiesIntents.getIntentsDetails(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "GetIntents", devidString,
                    logMap.put("Num Intents", intentDetailsList.getIntentNames().size()));
            return intentDetailsList.setSuccessStatus();
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
                this.logger.logUserTraceEvent(LOGFROM, "GetIntent - AI not found for devId", devidString, logMap);
                return ApiError.getNotFound("AI not found for this Dev ID");
            }

            ApiIntent intent = this.databaseEntitiesIntents.getIntent(aiid, intentName);
            if (null == intent) {
                this.logger.logUserTraceEvent(LOGFROM, "GetIntent - not found", devidString, logMap);
                return ApiError.getNotFound("Intent not found");
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

    public ApiResult writeIntent(final UUID devid, final UUID aiid, final ApiIntent intent) {
        String devidString = devid.toString();
        LogMap logMap = LogMap.map("AIID", aiid).put("IntentName", intent.getIntentName());

        // Make sure we don't have variables set with lifetime = 0 turns since this would immediately
        // delete the variable on the first turn, render it useless.
        for (IntentVariable v: intent.getVariables()) {
            if (v.getLifetimeTurns() == 0) {
                v.setLifetimeTurns(-1);
            }
        }

        try {
            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.jsonSerializer);
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - Bot is RO", devidString, logMap);
                return ApiError.getBadRequest(AILogic.BOT_RO_MESSAGE);
            }

            final boolean created = this.databaseEntitiesIntents.getIntent(aiid, intent.getIntentName()) == null;

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
                            String.format("Unlabeled variable: %s.", firstEmptyLabelVar.get().getEntityName()));
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
            try (DatabaseTransaction transaction = this.databaseTransactionProvider.get()) {
                this.databaseEntitiesIntents.writeIntent(devid, aiid, intent.getIntentName(), intent, transaction);
                WebHook webHook = intent.getWebHook();
                if (webHook != null) {
                    if (this.databaseEntitiesIntents.getWebHook(aiid, intent.getIntentName()) != null) {
                        if (!this.databaseAi.updateWebHook(aiid, intent.getIntentName(),
                                webHook.getEndpoint(), webHook.isEnabled(), transaction)) {
                            this.logger.logUserErrorEvent(LOGFROM, "Failed to update webhook",
                                    devidString, logMap);
                            return ApiError.getInternalServerError();
                        }
                        this.logger.logUserTraceEvent(LOGFROM, "UpdateWebHook", devidString, logMap);
                    } else {
                        if (!this.databaseAi.createWebHook(aiid, webHook.getIntentName(),
                                webHook.getEndpoint(), webHook.isEnabled(), transaction)) {
                            this.logger.logUserErrorEvent(LOGFROM, "Failed to create webhook",
                                    devidString, logMap);
                            return ApiError.getInternalServerError();
                        }
                        this.logger.logUserTraceEvent(LOGFROM, "WriteWebHook", devidString, logMap);
                    }
                }
                transaction.commit();
            }
            this.trainingLogic.stopTraining(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent", devidString, logMap);
            if (created) {
                return new ApiResult().setCreatedStatus("Intent created.");
            } else {
                return new ApiResult().setSuccessStatus("Intent updated.");
            }
        } catch (DatabaseEntitiesIntents.DatabaseEntityException dmee) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - entity duplicate or non existent", devidString,
                    logMap.put("Message", dmee.getMessage()));
            return ApiError.getBadRequest("Duplicate or missing entity_name.");
        } catch (DatabaseIntegrityViolationException dive) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - attempt to rename existing name",
                    devidString, logMap);
            return ApiError.getBadRequest("Intent name already in use.");
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WriteIntent", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteIntent(final UUID devid, final UUID aiid, final String intentName) {
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


