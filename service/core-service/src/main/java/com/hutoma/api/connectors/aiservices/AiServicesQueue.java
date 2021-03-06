package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.*;
import com.hutoma.api.connectors.db.Database;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.ServiceIdentity;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.containers.sub.TrainingStatus;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.thread.ITrackedThreadSubPool;
import org.glassfish.jersey.client.JerseyClient;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class AiServicesQueue extends ServerConnector {

    private static final String LOGFROM = "aiservicesqueue";
    private static final String COMMAND_PARAM = "command";

    private final Database database;

    @Inject
    public AiServicesQueue(final Database database,
                           final ILogger logger,
                           final IConnectConfig connectConfig,
                           final JerseyClient jerseyClient,
                           final JsonSerializer serializer,
                           final Tools tools,
                           final ITrackedThreadSubPool threadSubPool) {
        super(logger, connectConfig, serializer, tools, jerseyClient, threadSubPool);
        this.database = database;
    }

    /***
     * QUeue a command to start training
     * @param status
     * @param serverType
     * @param aiIdentity
     * @throws DatabaseException
     */
    void userActionStartTraining(final BackendStatus status,
                                 final BackendServerType serverType,
                                 final AiIdentity aiIdentity)
            throws DatabaseException {
        // get the current status
        BackendEngineStatus engineStatus = status.getEngineStatus(serverType);
        ServiceIdentity serviceIdentity =
                new ServiceIdentity(serverType, aiIdentity.getLanguage(), aiIdentity.getServerVersion());
        // set the status to training_queued without changing the progress
        this.database.updateAIStatus(serviceIdentity,
                aiIdentity.getAiid(), TrainingStatus.AI_TRAINING_QUEUED, "",
                engineStatus.getTrainingProgress(), engineStatus.getTrainingError());
        // queue this AI for training
        this.database.queueUpdate(serviceIdentity, aiIdentity.getAiid(), true, 0, QueueAction.TRAIN);
    }

    /***
     * Stop training now (not a queued action)
     * @param backendStatus
     * @param serverType
     * @param controller
     * @param aiIdentity
     * @throws DatabaseException
     * @throws ServerConnector.AiServicesException
     */
    void userActionStopTraining(final BackendStatus backendStatus,
                                final BackendServerType serverType,
                                final ControllerConnector controller,
                                final AiIdentity aiIdentity)
            throws DatabaseException, ServerConnector.AiServicesException {
        // stop training, and save "stopped" state in the dabatase
        stopTrainingIfActive(backendStatus, serverType, controller, aiIdentity, true);
    }

    /***
     * Stop training if the AI is in a state where it reasonably could be training
     * @param backendStatus
     * @param serverType
     * @param controller
     * @param aiIdentity
     * @param setDbStatus whether to save the stop state in the database.
     *                    TRUE if this is a stop command, FALSE if it part of a different command e.g. a delete
     * @throws DatabaseException
     * @throws AiServicesException
     */
    private void stopTrainingIfActive(final BackendStatus backendStatus,
                                      final BackendServerType serverType,
                                      final ControllerConnector controller,
                                      final AiIdentity aiIdentity,
                                      boolean setDbStatus) throws DatabaseException, AiServicesException {

        // get an endpoint map, i.e. a map from serverIdentifier to the actual servertracker object
        Map<String, ServerTrackerInfo> map = controller.getVerifiedEndpointMap(
                aiIdentity.getLanguage(), ServiceIdentity.DEFAULT_VERSION, serializer);
        // get the status of the AI for the backend server we are dealing with
        BackendEngineStatus status = backendStatus.getEngineStatus(serverType);

        // get a tracker if there is one (meaning the AI might have started training)
        ServerTrackerInfo tracker = null;
        if (status != null) {
            String endpoint = status.getServerIdentifier();
            if (endpoint != null && !endpoint.isEmpty()) {
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
                            break;
                    }
                    // copy the old fields but set new status
                    this.database.updateAIStatus(
                            new ServiceIdentity(serverType, aiIdentity.getLanguage(), aiIdentity.getServerVersion()),
                            aiIdentity.getAiid(), newStatus,
                            status.getServerIdentifier(), status.getTrainingProgress(), status.getTrainingError());
                }
            }

            // if we get here we know this is not null otherwise the AiServicesException is thrown, but
            // we need this check to appease the static tools
            if (tracker != null) {
                this.stopTrainingDirect(serverType, aiIdentity,
                        tracker.getServerUrl(), tracker.getServerIdentifier());
            }
        }
    }

    /***
     * Queue a task to delete an AI
     * @param backendStatus
     * @param serverType
     * @param controller
     * @param aiIdentity
     * @throws DatabaseException
     * @throws ServerConnector.AiServicesException
     */
    void userActionDelete(final BackendStatus backendStatus,
                          final BackendServerType serverType,
                          final ControllerConnector controller,
                          final AiIdentity aiIdentity)
            throws DatabaseException, ServerConnector.AiServicesException {
        // if we are training then stop immediately
        stopTrainingIfActive(backendStatus, serverType, controller, aiIdentity, true);
        // queue the action to delete, only run this some time in the future after the ai is fully stopped
        this.database.queueUpdate(
                new ServiceIdentity(serverType, aiIdentity.getLanguage(), aiIdentity.getServerVersion()),
                aiIdentity.getAiid(), true, 10, QueueAction.DELETE);
    }

    /***
     * Set the correct ai status and queue state after an upload
     * @param backendStatus
     * @param serverType
     * @param controller
     * @param aiIdentity
     * @throws DatabaseException
     * @throws AiServicesException
     */
    void userActionUpload(final BackendStatus backendStatus,
                          final BackendServerType serverType,
                          final ControllerConnector controller,
                          final AiIdentity aiIdentity)
            throws DatabaseException, AiServicesException {
        ServiceIdentity serviceIdentity = new ServiceIdentity(serverType, aiIdentity.getLanguage(),
                aiIdentity.getServerVersion());
        // send a stop training command if necessary
        stopTrainingIfActive(backendStatus, serverType, controller, aiIdentity, false);
        // set the status to undefined while we upload.
        // when the back-end is ready it will call back to say ready_to_train
        this.database.updateAIStatus(serviceIdentity, aiIdentity.getAiid(),
                TrainingStatus.AI_UNDEFINED, "", 0.0, 9999.0);
        // clear the queue state
        this.database.queueUpdate(serviceIdentity, aiIdentity.getAiid(), false, 0, QueueAction.NONE);
    }

    /***
     * Call from queued task to backend to stop training
     *
     * @param serverType
     * @param aiIdentity
     * @param serverEndpoint
     * @param serverIdentifier
     * @throws AiServicesException
     */
    private void stopTrainingDirect(final BackendServerType serverType,
                                    final AiIdentity aiIdentity,
                                    final String serverEndpoint,
                                    final String serverIdentifier)
            throws AiServicesException {

        LogMap logMap = LogMap.map("Op", "train-stop")
                .put("Type", serverType.value())
                .put("Server", serverIdentifier)
                .put("AIID", aiIdentity.getAiid());
        this.logger.logUserInfoEvent(LOGFROM,
                String.format("Sending \"stop\" %s to %s", aiIdentity.getAiid().toString(), serverType.value()),
                aiIdentity.getDevId().toString(), logMap);

        HashMap<String, Callable<InvocationResult>> callables =
                getTrainingCallableForEndpoint(aiIdentity, serverEndpoint,
                        new HashMap<String, String>() {{
                            put(COMMAND_PARAM, "stop");
                        }});
        executeAndWait(callables);
    }
}
