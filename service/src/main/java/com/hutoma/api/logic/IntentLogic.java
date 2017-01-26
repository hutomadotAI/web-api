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
import javax.ws.rs.core.SecurityContext;

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

    public ApiResult getIntents(SecurityContext securityContext, String devid, UUID aiid) {
        try {
            this.logger.logDebug(LOGFROM, "request to list intents for " + aiid.toString() + " from " + devid);
            final List<String> intentList = this.database.getIntents(devid, aiid);
            if (intentList.isEmpty()) {
                return ApiError.getNotFound();
            }
            return new ApiIntentList(aiid, intentList).setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getIntent(SecurityContext securityContext, String devid, UUID aiid, String intentName) {
        try {
            this.logger.logDebug(LOGFROM, "request to list intent " + intentName + " for AI " + aiid);
            ApiIntent intent = this.database.getIntent(devid, aiid, intentName);
            if (null == intent) {
                return ApiError.getNotFound();
            }
            return intent.setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult writeIntent(String devid, UUID aiid, ApiIntent intent) {
        try {
            this.logger.logDebug(LOGFROM, "request to edit intent " + intent.getIntentName() + " from " + devid);
            this.database.writeIntent(devid, aiid, intent.getIntentName(), intent);
            this.trainingLogic.stopTraining(devid, aiid);
            return new ApiResult().setSuccessStatus();
        } catch (DatabaseEntitiesIntents.DatabaseEntityException dmee) {
            this.logger.logDebug(LOGFROM, "entity " + dmee.getMessage() + " duplicated or non-existent");
            return ApiError.getBadRequest("duplicate or missing entity_name");
        } catch (Database.DatabaseIntegrityViolationException dive) {
            this.logger.logDebug(LOGFROM, "attempt to rename to existing name");
            return ApiError.getBadRequest("intent name already in use");
        } catch (final Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult deleteIntent(String devid, UUID aiid, String intentName) {
        try {
            this.logger.logDebug(LOGFROM, "request to delete intent " + intentName + " from " + devid);
            if (!this.database.deleteIntent(devid, aiid, intentName)) {
                return ApiError.getNotFound();
            }
            this.trainingLogic.stopTraining(devid, aiid);
            return new ApiResult().setSuccessStatus();
        } catch (final Exception e) {
            this.logger.logException(LOGFROM, e);
            return ApiError.getInternalServerError();
        }
    }
}


