package com.hutoma.api.logic;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiServerAcknowledge;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerRegistration;

import javax.inject.Inject;

/**
 * Created by David MG on 01/02/2017.
 */
public class AIServicesLogic {

    private static final String LOGFROM = "ailogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final AIServices aiServices;
    private final AiServiceStatusLogger serviceStatusLogger;
    private final Tools tools;

    @Inject
    public AIServicesLogic(Config config, JsonSerializer jsonSerializer, Database database, AIServices aiServices,
                           AiServiceStatusLogger serviceStatusLogger, Tools tools) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.serviceStatusLogger = serviceStatusLogger;
        this.tools = tools;
        this.aiServices = aiServices;
    }

    public ApiResult updateAIStatus(final AiStatus status) {
        try {
            this.serviceStatusLogger.logStatusUpdate(LOGFROM, status);
            // Check if any of the backends sent a rogue double, as MySQL does not handle NaN
            if (Double.isNaN(status.getTrainingError()) || Double.isNaN(status.getTrainingProgress())) {
                this.serviceStatusLogger.logError(LOGFROM, String.format("%s sent a NaN for AI %s",
                        status.getAiEngine(), status.getAiid()));
                return ApiError.getBadRequest("Double sent is NaN");
            }
            if (!this.database.updateAIStatus(status, this.jsonSerializer)) {
                this.serviceStatusLogger.logError(LOGFROM, String.format("%s sent a an update for unknown AI %s",
                        status.getAiEngine(), status.getAiid()));
                return ApiError.getNotFound();
            }
            return new ApiResult().setSuccessStatus();
        } catch (Database.DatabaseException ex) {
            this.serviceStatusLogger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult registerServer(ServerRegistration registration) {
        return new ApiServerAcknowledge(this.tools.createNewRandomUUID()).setSuccessStatus("registered");
    }

    public ApiResult updateAffinity(final ServerAffinity serverAffinity) {
        return new ApiResult().setSuccessStatus("server affinity updated");
    }
}
