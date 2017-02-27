package com.hutoma.api.logic;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiServerAcknowledge;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerBase;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

/**
 * Created by David MG on 01/02/2017.
 */
public class AIServicesLogic {

    private static final String LOGFROM = "aiserviceslogic";
    private final Config config;
    private final JsonSerializer jsonSerializer;
    private final Database database;
    private final AIServices aiServices;
    private final AiServiceStatusLogger serviceStatusLogger;
    private final ILogger logger;
    private final Tools tools;
    ControllerWnet controllerWnet;
    ControllerRnn controllerRnn;
    ControllerAiml controllerAiml;


    @Inject
    public AIServicesLogic(final Config config, final JsonSerializer jsonSerializer,
                           final Database database, final AIServices aiServices,
                           final AiServiceStatusLogger serviceStatusLogger, ILogger logger,
                           final Tools tools,
                           final ControllerWnet controllerWnet, final ControllerRnn controllerRnn,
                           final ControllerAiml controllerAiml) {
        this.config = config;
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.aiServices = aiServices;
        this.serviceStatusLogger = serviceStatusLogger;
        this.tools = tools;
        this.controllerWnet = controllerWnet;
        this.controllerRnn = controllerRnn;
        this.controllerAiml = controllerAiml;
        this.logger = logger;
    }

    public ApiResult updateAIStatus(final AiStatus status) {

        try {

            // Check if any of the backends sent a rogue double, as MySQL does not handle NaN
            if (Double.isNaN(status.getTrainingError()) || Double.isNaN(status.getTrainingProgress())) {
                this.serviceStatusLogger.logError(LOGFROM, String.format("%s sent a NaN for AI %s",
                        status.getAiEngine(), status.getAiid()));
                return ApiError.getBadRequest("Double sent is NaN");
            }

            // figure out which controller this is for
            ControllerBase controller = getControllerFor(status.getAiEngine());

            // if the session is not active, log and return an error
            if (!controller.isActiveSession(status.getServerSessionID())) {

                this.serviceStatusLogger.logWarning(LOGFROM,
                        String.format("update received from %s for AI %s using bad session ID",
                                status.getAiEngine(), status.getAiid()));
                return ApiError.getBadRequest("nonexistent session");
            }

            // is the update coming from the primary master?
            if (!controller.isPrimaryMaster(status.getServerSessionID())) {
                this.serviceStatusLogger.logWarning(LOGFROM,
                        String.format("ignoring update received from %s for AI %s because it is not the primary master",
                                status.getAiEngine(), status.getAiid()));
                return ApiError.getBadRequest("only the primary master can send status updates.");
            }

            // we accept the update. log it.
            this.serviceStatusLogger.logStatusUpdate(LOGFROM, "UpdateAIStatus", status);

            if (!this.database.updateAIStatus(status, this.jsonSerializer)) {
                this.serviceStatusLogger.logError(LOGFROM, String.format("%s sent an update for unknown AI %s",
                        status.getAiEngine(), status.getAiid()));
                return ApiError.getNotFound();
            }

            // update the ai hashcode
            controller.setHashCodeFor(status.getAiid(), status.getAiHash());
            return new ApiResult().setSuccessStatus();

        } catch (Exception ex) {
            this.serviceStatusLogger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult registerServer(ServerRegistration registration) {
        UUID serverSessionID;
        try {
            ControllerBase controller = getControllerFor(registration.getServerType());
            if (controller != null) {
                // create a session and make it active
                serverSessionID = controller.registerServer(registration);
                // if we are master then update all the hash codes from this reg-list
                if (controller.isPrimaryMaster(serverSessionID)) {
                    controller.setAllHashCodes(registration.getAiList());
                }
            } else {
                this.serviceStatusLogger.logError(LOGFROM,
                        String.format("unrecognised server type %s", registration.getServerType()));
                return ApiError.getBadRequest("unrecognised server type");
            }
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
        return new ApiServerAcknowledge(serverSessionID).setSuccessStatus("registered");
    }

    public ApiResult updateAffinity(final ServerAffinity serverAffinity) {
        UUID sid = serverAffinity.getServerSessionID();
        List<UUID> aiList = serverAffinity.getAiList();

        try {
            if (this.controllerWnet.updateAffinity(sid, aiList)
                    || this.controllerRnn.updateAffinity(sid, aiList)
                    || this.controllerAiml.updateAffinity(sid, aiList)) {
                return new ApiResult().setSuccessStatus("server affinity updated");
            }
            return ApiError.getBadRequest("nonexistent session");
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    private ControllerBase getControllerFor(BackendServerType server) {
        switch (server) {
            case WNET:
                return this.controllerWnet;
            case RNN:
                return this.controllerRnn;
            case AIML:
                return this.controllerAiml;
            default:
        }
        return null;
    }
}
