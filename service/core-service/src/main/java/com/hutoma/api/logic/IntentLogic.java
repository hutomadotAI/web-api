package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.CsvIntentReader;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.db.*;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.IntentConditionOperator;
import com.hutoma.api.containers.sub.IntentVariable;
import com.hutoma.api.containers.sub.IntentVariableCondition;
import com.hutoma.api.containers.sub.UserInfo;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Logic for handling intents creation/update/deletion
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
    private final CsvIntentReader csvIntentReader;
    private final DatabaseUser databaseUser;

    @Inject
    IntentLogic(final Config config,
                final ILogger logger,
                final DatabaseEntitiesIntents databaseEntitiesIntents,
                final DatabaseAI databaseAi,
                final TrainingLogic trainingLogic,
                final JsonSerializer jsonSerializer,
                final Provider<DatabaseTransaction> transactionProvider,
                final CsvIntentReader csvIntentReader,
                final DatabaseUser databaseUser) {
        this.config = config;
        this.logger = logger;
        this.databaseEntitiesIntents = databaseEntitiesIntents;
        this.databaseAi = databaseAi;
        this.trainingLogic = trainingLogic;
        this.jsonSerializer = jsonSerializer;
        this.databaseTransactionProvider = transactionProvider;
        this.csvIntentReader = csvIntentReader;
        this.databaseUser = databaseUser;
    }

    /**
     * Gets all the intents for a given bot.
     * @param devid the developer id owner of the bot
     * @param aiid  the ai id
     * @return list of intents for the bot
     */
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

    /**
     * Gets a given intents.
     * @param devid      the developer id owner of the bot
     * @param aiid       the ai id
     * @param intentName the intent name
     * @return the intent details
     */
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

            this.logger.logUserTraceEvent(LOGFROM, "GetIntent", devidString, logMap);

            return intent.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetIntent", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Writes an intent (creates/updates)
     * @param devid  the developer id
     * @param aiid   the ai id
     * @param intent the intent to write
     * @return the result of the operation
     */
    public ApiResult writeIntent(final UUID devid, final UUID aiid, final ApiIntent intent) {
        String devidString = devid.toString();
        LogMap logMap = LogMap.map("AIID", aiid).put("IntentName", intent.getIntentName());

        try {

            // Make sure we don't have variables set with lifetime = 0 turns since this would immediately
            // delete the variable on the first turn, render it useless.
            if (intent.getVariables() != null) {
                for (IntentVariable v : intent.getVariables()) {
                    if (v.getLifetimeTurns() == 0) {
                        v.setLifetimeTurns(-1);
                    }
                }
            }

            ApiAi ai = this.databaseAi.getAI(devid, aiid, this.jsonSerializer);
            if (ai.isReadOnly()) {
                this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - Bot is RO", devidString, logMap);
                return ApiError.getBadRequest(AILogic.BOT_RO_MESSAGE);
            }

            final boolean created = this.databaseEntitiesIntents.getIntent(aiid, intent.getIntentName()) == null;

            // Check if there are any variables with duplicate or empty labels
            ApiResult labelsResult = checkForDuplicateOrEmptyLabels(devid, aiid, intent);
            if (labelsResult.getStatus().getCode() != HttpURLConnection.HTTP_OK) {
                return labelsResult;
            }

            // Extract all the followup intents
            HashSet<String> followupIntents = new LinkedHashSet<>();
            if (intent.getIntentOutConditionals() != null) {
                intent.getIntentOutConditionals().forEach(x -> followupIntents.add(x.getIntentName()));
            }

            try (DatabaseTransaction transaction = this.databaseTransactionProvider.get()) {

                if (!followupIntents.isEmpty()) {
                    String conditionVarName = String.format("%s_complete", intent.getIntentName());
                    IntentVariableCondition condition = new IntentVariableCondition(
                            conditionVarName, IntentConditionOperator.SET, "");

                    boolean newIntentsCreated = false;
                    // Need to create new ones if they don't exist yet
                    for (String followupIntentName : followupIntents) {
                        ApiIntent followupIntent = this.databaseEntitiesIntents.getIntent(
                                aiid, followupIntentName, transaction);
                        if (followupIntent == null) {
                            ApiIntent newIntent = new ApiIntent(followupIntentName, "", "");
                            // Now add a conditional on the execution of this new intent based on the parent one
                            newIntent.setConditionsIn(Collections.singletonList(condition));
                            this.databaseEntitiesIntents.writeIntent(devid, aiid, followupIntentName,
                                    newIntent, transaction);
                            newIntentsCreated = true;
                        }
                    }

                    // Now if we've created new intents, we've added a new condition to them, so we need to
                    // set up the variable on the parent intent
                    if (newIntentsCreated) {
                        if (intent.getContextOut() == null) {
                            intent.setContextOut(new HashMap<>());
                        }
                        intent.getContextOut().put(conditionVarName, "");
                    }
                }

                // Now proceed with writing the main intent
                this.databaseEntitiesIntents.writeIntent(devid, aiid, intent.getIntentName(), intent, transaction);
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

    /**
     * Deletes an intent.
     * @param devid      the developer id
     * @param aiid       the ai is
     * @param intentName the intent name
     * @return the result of the operation
     */
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
            this.trainingLogic.stopTraining(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "DeleteIntent", devidString, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteIntent", devidString, e);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Bulk import intents from a CSV file.
     * @param devId               the developer id
     * @param aiid                the ai id
     * @param uploadedInputStream the stream for the file
     * @return the result of the operation
     */
    public ApiResult bulkImportFromCsv(final UUID devId,
                                       final UUID aiid,
                                       final InputStream uploadedInputStream) {
        try {
            long maxUploadFileSize = 1024L * this.config.getMaxUploadSizeKb();
            String fileContents = getFile(maxUploadFileSize, uploadedInputStream);
            ApiCsvImportResult results = this.csvIntentReader.parseIntents(fileContents);
            Set<String> intentNames = new LinkedHashSet<>();

            for (ApiCsvImportResult.ImportResultSuccess imported : results.getImported()) {
                if (intentNames.contains(imported.getIntentName())) {
                    return ApiError.getBadRequest(String.format("Duplicate intent name: %s", imported.getIntentName()));
                }
                intentNames.add(imported.getIntentName());
            }

            try (DatabaseTransaction transaction = this.databaseTransactionProvider.get()) {
                for (ApiCsvImportResult.ImportResultSuccess imported : results.getImported()) {
                    ApiIntent intent = imported.getIntent();
                    int retval = this.databaseEntitiesIntents.writeIntent(devId, aiid, intent.getIntentName(),
                            intent, transaction);
                    // writeIntent can only return 1 or 2
                    imported.setAction(retval == 1 ? "added" : "updated");
                }
                transaction.commit();
            }

            this.logger.logUserTraceEvent(LOGFROM, "CSV Import", devId.toString(),
                    LogMap.map("NumImported", results.getImported().size())
                            .put("NumErrors", results.getErrors().size())
                            .put("NumWarnings", results.getWarnings().size()));
            return results.setSuccessStatus();

        } catch (UploadTooLargeException ex) {
            this.logger.logUserInfoEvent(LOGFROM, "CSV upload too large", devId.toString());
            return ApiError.getBadRequest(
                    String.format("Upload too large. Maximum %dKb", this.config.getMaxUploadSizeKb()));
        } catch (Exception ex) {
            this.logger.logUserExceptionEvent(LOGFROM, "bulkimportcsv", devId.toString(), ex);
            return ApiError.getInternalServerError();
        }
    }

    @Deprecated
    public ApiResult convertIntentsToJson() {
        try (DatabaseTransaction transaction = this.databaseTransactionProvider.get()) {
            int intentCount = 0;
            List<UserInfo> users = this.databaseUser.getAllUsers();
            for (UserInfo userInfo: users) {
                UUID devId = UUID.fromString(userInfo.getDevId());
                List<ApiAi> ais = this.databaseAi.getAllAIs(devId, this.jsonSerializer);
                for (ApiAi ai: ais) {
                    UUID aiid = UUID.fromString(ai.getAiid());
                    List<String> intentNames = this.databaseEntitiesIntents.getIntents(devId, aiid);
                    for (String intentName: intentNames) {
                        intentCount++;
                        ApiIntent intent = this.databaseEntitiesIntents.getIntent_toDeprecate(
                                aiid, intentName, transaction);
                        this.databaseEntitiesIntents.writeIntent(
                                devId, aiid, intent.getIntentName(), intent, transaction);
                    }
                }
            }
            transaction.commit();
            return new ApiResult().setSuccessStatus(String.format("%d intents converted", intentCount));

        } catch (DatabaseException ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    /**
     * Gets the contents of the file from a stream taking into account a defined maximum upload size.
     * @param maxUploadSize       the maximum size allowed for the file
     * @param uploadedInputStream the input stream
     * @return the file contents as a string
     * @throws UploadTooLargeException when the file is too large (larger than maxUploadSize)
     * @throws IOException             when an IO exception occurs
     */
    private String getFile(final long maxUploadSize, final InputStream uploadedInputStream)
            throws UploadTooLargeException, IOException {

        StringBuilder sb = new StringBuilder();
        long fileSize = 0;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(uploadedInputStream, StandardCharsets.UTF_8));
            String line;
            int lineSize;
            while ((line = reader.readLine()) != null) {
                lineSize = line.length() + 2;
                // if the line doesn't push us over the upload limit
                if ((fileSize + lineSize) < maxUploadSize) {
                    fileSize += lineSize;
                } else {
                    throw new UploadTooLargeException();
                }
                sb.append(line);
                sb.append("\n");
            }
        } finally {
            if (null != reader) {
                reader.close();
            }
        }
        return sb.toString();
    }

    /**
     * Checks if there are any variables with duplicate or empty labels.
     * @param devId  the developer id
     * @param aiid   the ai id
     * @param intent the intent
     * @return the result
     */
    private ApiResult checkForDuplicateOrEmptyLabels(final UUID devId, final UUID aiid, final ApiIntent intent) {
        Set<String> usedLabels = new HashSet<>();
        List<String> duplicateLabels = new ArrayList<>();
        List<IntentVariable> variables = intent.getVariables();
        if (variables != null) {
            Optional<IntentVariable> firstEmptyLabelVar = variables.stream().filter(
                    x -> x.getLabel().isEmpty()).findFirst();
            if (firstEmptyLabelVar.isPresent()) {
                this.logger.logUserErrorEvent(LOGFROM, "Unlabeled variable", devId.toString(),
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
            this.logger.logUserErrorEvent(LOGFROM, "Duplicate labels", devId.toString(),
                    LogMap.map("DupLabels", dupLabelsString).put("AIID", aiid)
                            .put("IntentName", intent.getIntentName()));
            return ApiError.getBadRequest(
                    String.format("Duplicate label%s: %s", dupLabelsString.isEmpty() ? "" : "s", dupLabelsString));
        }
        return new ApiResult().setSuccessStatus();
    }
}


