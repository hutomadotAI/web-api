package com.hutoma.api.logic;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.BackendEngineStatus;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.QueueAction;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiServerAcknowledge;
import com.hutoma.api.containers.sub.AiStatus;
import com.hutoma.api.containers.sub.ServerAffinity;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.ServerRegistration;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerBase;
import com.hutoma.api.controllers.ControllerSvm;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.logging.AiServiceStatusLogger;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by David MG on 01/02/2017.
 */
public class AIServicesLogic {

    private static final String LOGFROM = "aiserviceslogic";
    private final JsonSerializer jsonSerializer;
    private final DatabaseAiStatusUpdates database;
    private final AiServiceStatusLogger serviceStatusLogger;
    private final ILogger logger;
    private final ControllerWnet controllerWnet;
    private final ControllerAiml controllerAiml;
    private final ControllerSvm controllerSvm;


    @Inject
    AIServicesLogic(final JsonSerializer jsonSerializer,
                           final DatabaseAiStatusUpdates database,
                           final AiServiceStatusLogger serviceStatusLogger, ILogger logger,
                           final ControllerWnet controllerWnet,
                           final ControllerAiml controllerAiml,
                           final ControllerSvm controllerSvm) {
        this.jsonSerializer = jsonSerializer;
        this.database = database;
        this.serviceStatusLogger = serviceStatusLogger;
        this.controllerWnet = controllerWnet;
        this.controllerAiml = controllerAiml;
        this.controllerSvm = controllerSvm;
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
            if (controller == null) {
                this.serviceStatusLogger.logError(LOGFROM,
                        "No registered controller for engine " + status.getAiEngine());
                return ApiError.getBadRequest("No registered controller for engine");
            }

            String serverIdentifier = controller.getSessionServerIdentifier(status.getServerSessionID());

            // if the session is not active, log and return an error
            if (serverIdentifier == null) {

                this.serviceStatusLogger.logWarning(LOGFROM,
                        String.format("update received from %s for AI %s using bad session ID",
                                status.getAiEngine(), status.getAiid()));
                return ApiError.getBadRequest("nonexistent session");
            }
            status.setServerIdentifier(serverIdentifier);

            try {
                // does the new status make sense?
                if (!checkIfStatusTransitionIsValid(status)) {
                    return ApiError.getNotFound();
                }

                // we accept the update. log it.
                this.serviceStatusLogger.logStatusUpdate(LOGFROM, status, true);

                // commit the update
                this.database.updateAIStatus(status);

                // update the ai hashcode
                controller.setHashCodeFor(status.getAiid(), status.getAiHash());

            } catch (StatusTransitionIgnoredException ignore) {
                this.serviceStatusLogger.logStatusUpdate(LOGFROM, status, false);
            }

            return new ApiResult().setSuccessStatus();

        } catch (StatusTransitionRejectedException rejected) {
            return ApiError.getBadRequest(rejected.getMessage());
        } catch (OriginatingServerRejectedException rejected) {
            return ApiError.getConflict(rejected.getMessage());
        } catch (Exception ex) {
            this.serviceStatusLogger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult registerServer(final ServerRegistration registration) {
        UUID serverSessionID;
        try {
            ControllerBase controller = getControllerFor(registration.getServerType());
            if (controller != null) {
                // create a session and make it active
                serverSessionID = controller.registerServer(registration);

                // log registration data
                LogMap logMap = LogMap.map("Op", "registerServer")
                        .put("PrimaryMaster", controller.getPrimaryMasterIdentifier())
                        .put("ServerType", registration.getServerType().value())
                        .put("ServerIdentifier", controller.getSessionServerIdentifier(serverSessionID));

                // if we are master then update all the hash codes from this reg-list
                if (controller.isPrimaryMaster(serverSessionID)) {
                    controller.setAllHashCodes(registration.getAiList());
                    synchroniseStatuses(controller, registration);
                    this.serviceStatusLogger.logInfo(LOGFROM,
                            "registered primary master and synced db statuses and hash codes",
                            logMap);
                } else {
                    this.serviceStatusLogger.logInfo(LOGFROM,
                            "registered secondary server",
                            logMap);
                }
                return new ApiServerAcknowledge(serverSessionID).setSuccessStatus("registered");
            } else {
                this.serviceStatusLogger.logError(LOGFROM,
                        String.format("unrecognised server type %s", registration.getServerType()));
                return ApiError.getBadRequest("unrecognised server type");
            }
        } catch (Exception ex) {
            this.serviceStatusLogger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult updateAffinity(final ServerAffinity serverAffinity) {
        UUID sid = serverAffinity.getServerSessionID();
        List<UUID> aiList = serverAffinity.getAiList();

        try {
            BackendServerType updated = null;
            if (this.controllerWnet.updateAffinity(sid, aiList)) {
                updated = BackendServerType.WNET;
            } else if (this.controllerAiml.updateAffinity(sid, aiList)) {
                updated = BackendServerType.AIML;
            }
            if (updated == null) {
                return ApiError.getBadRequest("nonexistent session");
            }

            this.serviceStatusLogger.logAffinityUpdate(LOGFROM, updated, serverAffinity);

            return new ApiResult().setSuccessStatus("server affinity updated");
        } catch (Exception ex) {
            this.logger.logException(LOGFROM, ex);
            return ApiError.getInternalServerError();
        }
    }

    /***
     * Load the old state and make sure the new one makes sense
     * @param statusUpdate
     * @return whether AI was found or not
     * @throws DatabaseException
     * @throws StatusTransitionRejectedException
     */
    private boolean checkIfStatusTransitionIsValid(final AiStatus statusUpdate)
            throws DatabaseException, StatusTransitionRejectedException, OriginatingServerRejectedException,
            StatusTransitionIgnoredException {

        // load the status
        BackendEngineStatus botStatus = this.database.getAiQueueStatus(statusUpdate.getAiEngine(),
                statusUpdate.getAiid());
        if ((botStatus == null) || botStatus.isDeleted()) {
            this.logger.logError(LOGFROM, String.format("received update %s for bot %s that no longer exists",
                    statusUpdate.getTrainingStatus().value(), statusUpdate.getAiid().toString()));
            // return false if the AI was not found, or was found to have been deleted
            return false;
        }

        // pull out the previous status
        TrainingStatus previousStatus = botStatus.getTrainingStatus();
        previousStatus = (previousStatus == null) ? TrainingStatus.AI_UNDEFINED : previousStatus;

        // if the bot is currently training
        // or just received an update that says "training"
        // then check that the update is from the right server
        if (previousStatus == TrainingStatus.AI_TRAINING
                || statusUpdate.getTrainingStatus() == TrainingStatus.AI_TRAINING) {
            rejectIfUpdateWasFromWrongServer(statusUpdate, botStatus);
        }

        switch (statusUpdate.getTrainingStatus()) {
            case AI_TRAINING_QUEUED:
                // if the backend is telling us to queue training
                // then it means that a bot has finished its training timeslice
                rejectIfPreviousStatusWasNot(previousStatus, statusUpdate,
                        new TrainingStatus[]{
                                TrainingStatus.AI_TRAINING,
                                TrainingStatus.AI_TRAINING_QUEUED});
                // so queue it again for another one
                this.database.queueUpdate(statusUpdate.getAiEngine(), statusUpdate.getAiid(),
                        true, 0, QueueAction.TRAIN);
                break;
            case AI_UNDEFINED:
                // there is no valid reason for the backend to tell us that
                // a bot should become undefined
                rejectIfPreviousStatusWasNot(previousStatus, statusUpdate,
                        new TrainingStatus[]{});
                break;
            case AI_TRAINING:
                // we should only get a training callback
                // if we had previously queued something to train
                // or it was training already
                rejectIfPreviousStatusWasNot(previousStatus, statusUpdate,
                        new TrainingStatus[]{
                                TrainingStatus.AI_TRAINING,
                                TrainingStatus.AI_TRAINING_QUEUED});
                this.database.queueUpdate(statusUpdate.getAiEngine(), statusUpdate.getAiid(),
                        false, 0, QueueAction.NONE);
                break;
            case AI_TRAINING_STOPPED:
                // backend acknowledges that we have halted training for a bot
                rejectIfPreviousStatusWasNot(previousStatus, statusUpdate,
                        new TrainingStatus[]{
                                TrainingStatus.AI_TRAINING,
                                TrainingStatus.AI_READY_TO_TRAIN,
                                TrainingStatus.AI_TRAINING_QUEUED,
                                TrainingStatus.AI_TRAINING_STOPPED});
                throw new StatusTransitionIgnoredException();
            case AI_TRAINING_COMPLETE:
                // we only accept COMPLETE if the ai was either recently queued or training
                // any other state means that we never told the backend to train in the first place
                rejectIfPreviousStatusWasNot(previousStatus, statusUpdate,
                        new TrainingStatus[]{
                                TrainingStatus.AI_TRAINING,
                                TrainingStatus.AI_TRAINING_QUEUED});
                this.database.queueUpdate(statusUpdate.getAiEngine(), statusUpdate.getAiid(),
                        false, 0, QueueAction.NONE);
                break;
            // other states are always valid
            case AI_ERROR:
            case AI_READY_TO_TRAIN:
            default:
                this.database.queueUpdate(statusUpdate.getAiEngine(), statusUpdate.getAiid(),
                        false, 0, QueueAction.NONE);
        }
        return true;
    }

    /***
     * If the bot is training then we know which server is supposed to be training it
     * When an update arrives we need to check that it came from the right server.
     * @param status
     * @param botStatus
     * @throws OriginatingServerRejectedException
     */
    private void rejectIfUpdateWasFromWrongServer(final AiStatus status, final BackendEngineStatus botStatus)
            throws OriginatingServerRejectedException {
        // ensure that a training update comes from
        // the server we allocated training to
        if (!botStatus.getServerIdentifier().equals(status.getServerIdentifier())) {

            // log the transition attempt
            this.logger.logWarning(LOGFROM,
                    String.format("ignoring status update from wrong server %s (expected %s) for bot %s",
                            status.getServerIdentifier(), botStatus.getServerIdentifier(),
                            status.getAiid().toString()));

            // reject with distinct HTTP error
            throw new OriginatingServerRejectedException("another server is training this bot");
        }
    }

    /***
     * Compare previous state to a given list of valid states
     * Throw an exception if there is no match
     * @param previousStatus
     * @param newStatus
     * @param trainingStatuses
     * @throws StatusTransitionRejectedException
     */
    private void rejectIfPreviousStatusWasNot(final TrainingStatus previousStatus,
                                              final AiStatus newStatus,
                                              final TrainingStatus[] trainingStatuses)
            throws StatusTransitionRejectedException {
        // stream the list and look for a match
        if (Arrays.stream(trainingStatuses).noneMatch(x -> x == previousStatus)) {
            // if not match
            // log the transition attempt
            this.logger.logWarning(LOGFROM,
                    String.format("ignoring unexpected status transition from %s to %s for bot %s",
                            previousStatus.value(), newStatus.getTrainingStatus().value(),
                            newStatus.getAiid()));
            // and throw an exception
            throw new StatusTransitionRejectedException(
                    String.format("unexpected status transition from %s to %s",
                            previousStatus.value(), newStatus.getTrainingStatus().value()));
        }
    }

    private void synchroniseStatuses(ControllerBase controller, ServerRegistration registration)
            throws DatabaseException {
        Map<UUID, ServerAiEntry> result =
                registration.getAiList().stream()
                        .collect(Collectors.toMap(ServerAiEntry::getAiid, Function.identity()));
        controller.synchroniseDBStatuses(this.database, this.jsonSerializer, registration.getServerType(), result);
    }

    private ControllerBase getControllerFor(BackendServerType server) {
        switch (server) {
            case WNET:
                return this.controllerWnet;
            case AIML:
                return this.controllerAiml;
            case SVM:
                return this.controllerSvm;
            default:
        }
        return null;
    }

    public static class StatusTransitionRejectedException extends Exception {
        StatusTransitionRejectedException(final String message) {
            super(message);
        }
    }

    public static class OriginatingServerRejectedException extends Exception {
        OriginatingServerRejectedException(final String message) {
            super(message);
        }
    }

    private static class StatusTransitionIgnoredException extends Exception {
    }

}
