package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Created by David MG on 05/10/2016.
 */
public class IntentLogic {

    private static final String LOGFROM = "intentlogic";
    private final Config config;
    private final ILogger logger;
    private final DatabaseEntitiesIntents database;
    private final TrainingLogic trainingLogic;

    @Inject
    public IntentLogic(final Config config, final ILogger logger, final DatabaseEntitiesIntents database,
                       final TrainingLogic trainingLogic) {
        this.config = config;
        this.logger = logger;
        this.database = database;
        this.trainingLogic = trainingLogic;
    }

    public ApiResult getIntents(final String devid, final UUID aiid) {
        try {
            final List<String> intentList = this.database.getIntents(devid, aiid);
            if (intentList.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "GetIntents", devid, "AIID", aiid.toString(),
                        "Num Intents", "0");
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetIntents", devid, "AIID", aiid.toString(), "Num Intents",
                    intentList.size());
            return new ApiIntentList(aiid, intentList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetIntents", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getIntent(final String devid, final UUID aiid, final String intentName) {
        try {
            ApiIntent intent = this.database.getIntent(devid, aiid, intentName);
            if (null == intent) {
                this.logger.logUserTraceEvent(LOGFROM, "GetIntent - not found", devid, "AIID", aiid.toString(),
                        "IntentName", intentName);
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetIntent", devid, "AIID", aiid.toString(),
                    "IntentName", intentName);
            return intent.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetIntent", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult writeIntent(String devid, UUID aiid, ApiIntent intent) {
        try {
            this.database.writeIntent(devid, aiid, intent.getIntentName(), intent);
            this.trainingLogic.stopTraining(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent", devid, "AIID", aiid.toString(), "Intent Name",
                    intent.getIntentName());
            return new ApiResult().setSuccessStatus();
        } catch (DatabaseEntitiesIntents.DatabaseEntityException dmee) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - entity duplicate or non existent", devid,
                    "AIID", aiid.toString(), "Intent Name", intent.getIntentName(), "Message", dmee.getMessage());
            return ApiError.getBadRequest("duplicate or missing entity_name");
        } catch (Database.DatabaseIntegrityViolationException dive) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - attempt to rename existing name", devid,
                    "AIID", aiid.toString(), "Intent Name", intent.getIntentName());
            return ApiError.getBadRequest("intent name already in use");
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WriteIntent", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteIntent(String devid, UUID aiid, String intentName) {
        try {
            if (!this.database.deleteIntent(devid, aiid, intentName)) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteIntent - not found", devid,
                        "AIID", aiid.toString(), "Intent Name",
                        intentName);
                return ApiError.getNotFound();
            }
            this.trainingLogic.stopTraining(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "DeleteIntent", devid,
                    "AIID", aiid.toString(), "Intent Name", intentName);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteIntent", devid, e);
            return ApiError.getInternalServerError();
        }
    }
}


