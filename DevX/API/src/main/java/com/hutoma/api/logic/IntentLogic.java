package com.hutoma.api.logic;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.Logger;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiIntent;
import com.hutoma.api.containers.ApiIntentList;
import com.hutoma.api.containers.ApiResult;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.UUID;

/**
 * Created by David MG on 05/10/2016.
 */
public class IntentLogic {

    private final Config config;
    private final Logger logger;
    private final Database database;
    private static final String LOGFROM = "intentlogic";

    @Inject
    public IntentLogic(final Config config, final Logger logger, final Database database) {
        this.config = config;
        this.logger = logger;
        this.database = database;
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
            this.logger.logError(LOGFROM, "error getting intents: " + e.toString());
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
            this.logger.logError(LOGFROM, "error getting intent: " + e.toString());
            return ApiError.getInternalServerError();
        }
    }
}
