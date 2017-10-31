package com.hutoma.api.connectors;

import com.google.common.base.Strings;
import com.hutoma.api.common.Config;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.common.Tools;
import com.hutoma.api.thread.TrackedThreadSubPool;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.connectors.db.DatabaseUser;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.BackendStatus;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.containers.sub.QueueAction;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.controllers.ControllerBase;
import com.hutoma.api.controllers.InvocationResult;
import com.hutoma.api.controllers.ServerTracker;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.inject.Inject;

public class AIQueueServices extends ServerConnector {

    private static final String LOGFROM = "aiqueueservices";
    private static final String COMMAND_PARAM = "command";
    private static final String TRAINING_TIME_ALLOWED_PARAM = "training_time_allowed";

    private final DatabaseAiStatusUpdates databaseAiStatusUpdates;
    private final DatabaseUser databaseUser;

    @Inject
    public AIQueueServices(final DatabaseAiStatusUpdates database, final ILogger logger,
                           final JsonSerializer serializer, final Tools tools, final Config config,
                           final JerseyClient jerseyClient, final TrackedThreadSubPool threadSubPool,
                           final DatabaseUser databaseUser) {
        super(database, databaseUser, logger, serializer, tools, config, jerseyClient, threadSubPool);
        this.databaseAiStatusUpdates = database;
        this.databaseUser = databaseUser;
    }

    /***
     * Call from queued task to backend to delete an AI
     *
     * @param serverType
     * @param devId
     * @param aiid
     * @param endpoint
     * @param serverIdentifier
     * @throws AiServicesException
     */
    public void deleteAIDirect(final BackendServerType serverType, final UUID devId, final UUID aiid,
                               final String endpoint, final String serverIdentifier) throws AiServicesException {
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        final String devIdString = devId.toString();
        LogMap logMap = LogMap.map("Op", "delete")
                .put("Type", serverType.value())
                .put("Server", serverIdentifier)
                .put("AIID", aiid);
        this.logger.logUserInfoEvent(LOGFROM,
                String.format("Sending \"delete\" %s to %s", aiid.toString(), serverType.value()),
                devIdString, logMap);

        callables.put(endpoint, () -> new InvocationResult(
                aiid,
                this.jerseyClient
                        .target(endpoint).path(devIdString).path(aiid.toString())
                        .request()
                        .delete(),
                endpoint,
                0));
        executeAndWait(callables);
    }

    /***
     * Call from queued task to backend to start training
     *
     * @param serverType
     * @param devId
     * @param aiid
     * @param serverUrl
     * @param serverIdentifier
     * @throws AiServicesException
     */
    public void startTrainingDirect(final BackendServerType serverType,
                                    final UUID devId, final UUID aiid,
                                    final String serverUrl, final String serverIdentifier) throws AiServicesException {
        // first get the devplan to load training parameters
        DevPlan devPlan;
        try {
            devPlan = this.databaseUser.getDevPlan(devId);
        } catch (DatabaseException ex) {
            throw new AiServicesException("Could not get plan for devId " + devId);
        }

        LogMap logMap = LogMap.map("Op", "train-start")
                .put("Type", serverType.value())
                .put("Server", serverIdentifier)
                .put("AIID", aiid);
        this.logger.logUserInfoEvent(LOGFROM,
                String.format("Sending \"start\" %s to %s", aiid.toString(), serverType.value()),
                devId.toString(), logMap);

        HashMap<String, Callable<InvocationResult>> callables = getTrainingCallableForEndpoint(devId, aiid, serverUrl,
                new HashMap<String, String>() {{
                    put(COMMAND_PARAM, "start");
                    put(TRAINING_TIME_ALLOWED_PARAM, Integer.toString(devPlan.getMaxTrainingMins()));
                }});
        executeAndWait(callables);
    }

    /***
     * Call from queued task to backend to stop training
     *
     * @param serverType
     * @param devId
     * @param aiid
     * @param serverEndpoint
     * @param serverIdentifier
     * @throws AiServicesException
     */
    private void stopTrainingDirect(final BackendServerType serverType, final UUID devId, final UUID aiid,
                                    final String serverEndpoint, final String serverIdentifier)
            throws AiServicesException {

        LogMap logMap = LogMap.map("Op", "train-stop")
                .put("Type", serverType.value())
                .put("Server", serverIdentifier)
                .put("AIID", aiid);
        this.logger.logUserInfoEvent(LOGFROM,
                String.format("Sending \"stop\" %s to %s", aiid.toString(), serverType.value()),
                devId.toString(), logMap);

        HashMap<String, Callable<InvocationResult>> callables =
                getTrainingCallableForEndpoint(devId, aiid, serverEndpoint, new HashMap<String, String>() {{
                    put(COMMAND_PARAM, "stop");
                }});
        executeAndWait(callables);
    }

    /***
     * Assemble endpoint url to start or stop training
     * @param devId
     * @param aiid
     * @param endpoint
     * @param params
     * @return
     * @throws AiServicesException
     */
    private HashMap<String, Callable<InvocationResult>> getTrainingCallableForEndpoint(
            final UUID devId, final UUID aiid, final String endpoint, Map<String, String> params)
            throws AiServicesException {
        HashMap<String, Callable<InvocationResult>> callables = new HashMap<>();
        JerseyWebTarget target = this.jerseyClient.target(endpoint).path(devId.toString()).path(aiid.toString());
        for (Map.Entry<String, String> param : params.entrySet()) {
            target = target.queryParam(param.getKey(), param.getValue());
        }

        final JerseyInvocation.Builder builder = target.request();
        callables.put(endpoint, () -> new InvocationResult(aiid, builder.post(null), endpoint, 0));
        return callables;
    }

    /***
     * Stop training if the AI is in a state where it reasonably could be training
     * @param backendStatus
     * @param serverType
     * @param controller
     * @param devid
     * @param aiid
     * @param setDbStatus whether to save the stop state in the database.
     *                    TRUE if this is a stop command, FALSE if it part of a different command e.g. a delete
     * @throws DatabaseException
     * @throws AiServicesException
     */
    private void stopTrainingIfActive(final BackendStatus backendStatus, final BackendServerType serverType,
                                      final ControllerBase controller,
                                      final UUID devid, final UUID aiid,
                                      boolean setDbStatus) throws DatabaseException, AiServicesException {

        // get an endpoint map, i.e. a map from serverIdentifier to the actual servertracker object
        Map<String, ServerTracker> map = controller.getVerifiedEndpointMap();
        // get the status of the AI for the backend server we are dealing with
        BackendEngineStatus status = backendStatus.getEngineStatus(serverType);

        // get a tracker if there is one (meaning the AI might have started training)
        ServerTracker tracker = null;
        if (status != null) {
            String endpoint = status.getServerIdentifier();
            if (!Strings.isNullOrEmpty(endpoint)) {
                tracker = map.get(endpoint);
            }

            // if we want to save this state to the DB
            if (setDbStatus) {
                TrainingStatus newStatus = status.getTrainingStatus();
                if (newStatus != null) {
                    switch (newStatus) {
                        case AI_TRAINING:
                        case AI_TRAINING_QUEUED:
                        case AI_READY_TO_TRAIN:
                            newStatus = TrainingStatus.AI_TRAINING_STOPPED;
                            break;
                        default:
                    }
                    // copy the old fields but set new status
                    this.databaseAi.updateAIStatus(serverType, aiid, newStatus,
                            status.getServerIdentifier(), status.getTrainingProgress(), status.getTrainingError());
                }
            }
        }

        // if we have a tracker then tell the training server to stop
        if (tracker != null) {
            this.stopTrainingDirect(serverType, devid, aiid,
                    tracker.getServerUrl(), tracker.getServerIdentifier());
        }
    }

    /***
     * Set the correct ai status and queue state after an upload
     * @param backendStatus
     * @param serverType
     * @param controller
     * @param devid
     * @param aiid
     * @throws DatabaseException
     * @throws AiServicesException
     */
    void userActionUpload(final BackendStatus backendStatus, BackendServerType serverType,
                          ControllerBase controller, UUID devid, UUID aiid)
            throws DatabaseException, AiServicesException {
        // send a stop training command if necessary
        stopTrainingIfActive(backendStatus, serverType, controller, devid, aiid, false);
        // set the status to undefined while we upload.
        // when the back-end is ready it will call back to say ready_to_train
        this.databaseAiStatusUpdates.updateAIStatus(serverType, aiid,
                TrainingStatus.AI_UNDEFINED, "", 0.0, 9999.0);
        // clear the queue state
        this.databaseAiStatusUpdates.queueUpdate(serverType, aiid, false, 0, QueueAction.NONE);
    }

    /***
     * QUeue a command to start training
     * @param status
     * @param serverType
     * @param devid
     * @param aiid
     * @throws DatabaseException
     */
    void userActionStartTraining(BackendStatus status, BackendServerType serverType, UUID devid, UUID aiid)
            throws DatabaseException {
        // get the current status
        BackendEngineStatus engineStatus = status.getEngineStatus(serverType);
        // set the status to training_queued without changing the progress
        this.databaseAiStatusUpdates.updateAIStatus(serverType, aiid, TrainingStatus.AI_TRAINING_QUEUED, "",
                engineStatus.getTrainingProgress(), engineStatus.getTrainingError());
        // queue this AI for training
        this.databaseAiStatusUpdates.queueUpdate(serverType, aiid, true, 0, QueueAction.TRAIN);
    }

    /***
     * Stop training now (not a queued action)
     * @param backendStatus
     * @param serverType
     * @param controller
     * @param devid
     * @param aiid
     * @throws DatabaseException
     * @throws ServerConnector.AiServicesException
     */
    void userActionStopTraining(final BackendStatus backendStatus, BackendServerType serverType,
                                ControllerBase controller,
                                UUID devid, UUID aiid)
            throws DatabaseException, ServerConnector.AiServicesException {
        // stop training, and save "stopped" state in the dabatase
        stopTrainingIfActive(backendStatus, serverType, controller, devid, aiid, true);
    }

    /***
     * Queue a task to delete an AI
     * @param backendStatus
     * @param serverType
     * @param controller
     * @param devid
     * @param aiid
     * @throws DatabaseException
     * @throws ServerConnector.AiServicesException
     */
    void userActionDelete(final BackendStatus backendStatus, BackendServerType serverType, ControllerBase controller,
                          UUID devid, UUID aiid)
            throws DatabaseException, ServerConnector.AiServicesException {
        // if we are training then stop immediately
        stopTrainingIfActive(backendStatus, serverType, controller, devid, aiid, true);
        // queue the action to delete, only run this some time in the future after the ai is fully stopped
        this.databaseAiStatusUpdates.queueUpdate(serverType, aiid,
                true, 10, QueueAction.DELETE);
    }
}
