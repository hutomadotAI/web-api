package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseEntitiesIntents;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.sub.WebHook;

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
            LogMap logMap = LogMap.map("AIID", aiid);
            final List<String> intentList = this.database.getIntents(devid, aiid);
            if (intentList.isEmpty()) {
                this.logger.logUserTraceEvent(LOGFROM, "GetIntents", devid, logMap.put("Num Intents", "0"));
                return ApiError.getNotFound();
            }
            this.logger.logUserTraceEvent(LOGFROM, "GetIntents", devid,
                    logMap.put("Num Intents", intentList.size()));
            return new ApiIntentList(aiid, intentList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetIntents", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getIntent(final String devid, final UUID aiid, final String intentName) {
        try {
            LogMap logMap = LogMap.map("AIID", aiid).put("IntentName", intentName);
            ApiIntent intent = this.database.getIntent(devid, aiid, intentName);
            if (null == intent) {
                this.logger.logUserTraceEvent(LOGFROM, "GetIntent - not found", devid, logMap);
                return ApiError.getNotFound();
            }
            WebHook webHook = this.database.getWebHook(aiid, intentName);
            intent.setWebHook(webHook);

            this.logger.logUserTraceEvent(LOGFROM, "GetIntent", devid, logMap);

            return intent.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "GetIntent", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult writeIntent(String devid, UUID aiid, ApiIntent intent) {
        LogMap logMap = LogMap.map("AIID", aiid).put("IntentName", intent.getIntentName());
        try {
            this.database.writeIntent(devid, aiid, intent.getIntentName(), intent);
            WebHook webHook = intent.getWebHook();
            if (webHook != null) {
                if (this.database.getWebHook(aiid, intent.getIntentName()) != null) {
                    this.database.updateWebHook(aiid, intent.getIntentName(), webHook.getEndpoint(),
                            webHook.isEnabled());
                    this.logger.logUserTraceEvent(LOGFROM, "UpdateWebHook", devid, logMap);
                } else {
                    this.database.createWebHook(aiid, webHook.getIntentName(), webHook.getEndpoint(),
                            webHook.isEnabled());
                    this.logger.logUserTraceEvent(LOGFROM, "WriteWebHook", devid, logMap);
                }
            }
            this.trainingLogic.stopTraining(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent", devid, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (DatabaseEntitiesIntents.DatabaseEntityException dmee) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - entity duplicate or non existent", devid,
                    logMap.put("Message", dmee.getMessage()));
            return ApiError.getBadRequest("Duplicate or missing entity_name");
        } catch (Database.DatabaseIntegrityViolationException dive) {
            this.logger.logUserTraceEvent(LOGFROM, "WriteIntent - attempt to rename existing name", devid, logMap);
            return ApiError.getBadRequest("Intent name already in use");
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "WriteIntent", devid, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteIntent(String devid, UUID aiid, String intentName) {
        try {
            LogMap logMap = LogMap.map("AIID", aiid).put("IntentName", intentName);
            if (!this.database.deleteIntent(devid, aiid, intentName)) {
                this.logger.logUserTraceEvent(LOGFROM, "DeleteIntent - not found", devid, logMap);
                return ApiError.getNotFound();
            }
            if (this.database.getWebHook(aiid, intentName) != null) {
                this.database.deleteWebHook(aiid, intentName);
            }
            this.trainingLogic.stopTraining(devid, aiid);
            this.logger.logUserTraceEvent(LOGFROM, "DeleteIntent", devid, logMap);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logUserExceptionEvent(LOGFROM, "DeleteIntent", devid, e);
            return ApiError.getInternalServerError();
        }
    }
}


