package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.LogMap;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.AIQueueServices;
import com.hutoma.api.connectors.Database;
import com.hutoma.api.connectors.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.containers.sub.BackendEngineStatus;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.QueueAction;
import com.hutoma.api.containers.sub.ServerEndpointTrainingSlots;
import com.hutoma.api.containers.sub.TrainingStatus;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

/***
 * Only one of these should exist for each queuing controller.
 * i.e. one for WNET and one for RNN
 * When initialised, will start a thread that monitors the queue
 * and executes queued tasks
 */
public class QueueProcessor extends TimerTask {

    protected BackendServerType serverType;
    protected ControllerBase controller;
    private DatabaseAiStatusUpdates database;
    private AIQueueServices queueServices;
    private Tools tools;
    private ILogger logger;
    private String logFrom;
    private Config config;

    // flag to tell the inner thread not to quit
    private AtomicBoolean runQueueProcessor;
    // how long to wait until checking the queue again
    private AtomicLong runAgainAfterMs;

    // store the last state so that we know when it has changed
    private AtomicReference<String> lastKnownControllerState;

    // only used for logging
    private long lastRun = 0;
    private long lastKicked = 0;

    private Timer timer;

    @Inject
    public QueueProcessor(final Config config, final DatabaseAiStatusUpdates database, final AIQueueServices queueServices,
                          final Tools tools, ILogger logger) {
        this.config = config;
        this.logger = logger;
        this.database = database;
        this.tools = tools;
        this.queueServices = queueServices;
        this.runQueueProcessor = new AtomicBoolean(true);
        this.runAgainAfterMs = new AtomicLong(0);
        this.lastKnownControllerState = new AtomicReference<>("");
        this.timer = new Timer();
    }

    public void initialise(ControllerBase controller, final BackendServerType serverType) {
        this.serverType = serverType;
        this.controller = controller;
        this.logFrom = String.format("qproc-%s", serverType.value());
        // timer fires approx twice per second
        this.timer.schedule(this, 0, 500);
    }

    @Override
    public void run() {
        // check the run flag
        if (this.runQueueProcessor.get()) {
            try {
                // wait until we are ready to run again
                if (this.tools.getTimestamp() >= this.runAgainAfterMs.get()) {

                    long timeNow = this.tools.getTimestamp();
                    long sinceLastRun = timeNow - this.lastRun;
                    long sinceLastKick = timeNow - this.lastKicked;

                    this.logger.logDebug(this.logFrom, String.format("queue check: last check was %.1fs ago%s",
                            sinceLastRun / 1000.0,
                            (sinceLastRun <= sinceLastKick) ? "" :
                                    String.format(", kicked %.1fs ago", sinceLastKick / 1000.0)));
                    this.lastRun = timeNow;

                    // by default, wait a small amount of time to check again
                    // this may be changed inside processQueue
                    queueCheckAgainAfter(this.config.getProcessQueueIntervalDefault());

                    // process queue now
                    processQueue();
                }
            } catch (Exception e) {
                this.logger.logException(this.logFrom, e);
            }
        }
    }

    /***
     * If the queue processor was sleeping (long intervals)
     * then tell it to run soon
     */
    public void kickQueueProcessor() {

        long timeNow = this.tools.getTimestamp();
        this.lastKicked = timeNow;

        // ideally, we would run the queue at this time
        final long latestTimeToRun = timeNow + this.config.getProcessQueueIntervalShort();

        // only update the run time if we are bringing it forward not pushing it back
        this.runAgainAfterMs.getAndUpdate(previousTimeToRunNext ->
                (previousTimeToRunNext > latestTimeToRun) ? latestTimeToRun : previousTimeToRunNext);
    }

    private void queueCheckAgainAfter(long milliseconds) {
        this.runAgainAfterMs.set(this.tools.getTimestamp() + milliseconds);
    }

    private void storeServerStats(final int serverCount,
                                  final int totalTrainingCapacity, final int availableTrainingSlots,
                                  final int totalChatCapacity) {

        // create a string that represents the controller state completely
        String stateNow = String.format("servers:%d train_capacity:%d train_free:%d chat_capacity:%d",
                serverCount, totalTrainingCapacity, availableTrainingSlots, totalChatCapacity);

        // compare this state with the last state. was there a change? (update anyway)
        boolean changes = !this.lastKnownControllerState.getAndSet(stateNow).equals(stateNow);

        // ask the controller if we need to log a regular error if there is no capacity
        boolean errorIfNoTrainingCapacity = this.controller.logErrorIfNoTrainingCapacity();

        // if we detected a change ...
        if (changes) {
            // log the new state
            LogMap logMap = LogMap.map("Op", "heartbeat")
                    .put("Type", this.serverType)
                    .put("ServerCount", serverCount)
                    .put("TrainingCapacity", totalTrainingCapacity)
                    .put("TrainingSlotsAvailable", availableTrainingSlots)
                    .put("ChatCapacity", totalChatCapacity);
            this.logger.logInfo(this.logFrom, String.format("Controller state - %s", stateNow), logMap);

            // try to update the database with this state
            try {
                this.database.updateControllerState(this.serverType, serverCount,
                        totalTrainingCapacity, availableTrainingSlots, totalChatCapacity);
            } catch (Database.DatabaseException e) {
                this.logger.logException(this.logFrom, e);
                // if we didn't manage to update the db then change the string
                // so that it will come up as changed=true next time
                this.lastKnownControllerState.set("update failed");
            }
        }

        // if there is no training capacity log errors regularly (for some servers)
        if (errorIfNoTrainingCapacity && (totalTrainingCapacity < 1)) {
            this.logger.logError(this.logFrom, String.format("%s has zero training %scapacity",
                    this.serverType.value(), (totalChatCapacity < 1) ? "and chat " : ""));
        } else {
            // if there is no chat capacity then log errors regularly
            if (totalChatCapacity < 1) {
                this.logger.logError(this.logFrom, String.format("%s has zero chat capacity",
                        this.serverType.value()));
            }
        }
    }

    private ServerConnector.AiServicesException findFirstSuppressedHttpError(ServerConnector.AiServicesException e) {

        // usual null checks
        if (e == null || e.getSuppressed() == null) {
            return null;
        }
        // find the first AiServicesException
        return Arrays.stream(e.getSuppressed())
                .filter(error -> error instanceof ServerConnector.AiServicesException)
                .map(error -> (ServerConnector.AiServicesException) error)
                // with a non-zero http response code
                .filter(error -> error.getResponseStatus() > 0)
                .findFirst().orElse(null);
    }

    /***
     * * Deal with logging and requeuing or dropping when a delete task fails
     * @param queued
     * @param server
     * @param e
     */
    private void handleDeleteTaskFailure(final BackendEngineStatus queued, final ServerTracker server, final ServerConnector.AiServicesException e) {

        // requeue flag
        boolean requeueThis = true;
        String logMessage;
        // get the first suppressed error with a non-zero http error code
        ServerConnector.AiServicesException httpError = findFirstSuppressedHttpError(e);
        if (httpError != null) {
            switch (httpError.getResponseStatus()) {
                case HttpURLConnection.HTTP_NOT_FOUND:
                    requeueThis = false;
                    break;
                default:
                    break;
            }
            logMessage = httpError.getMessage();
        } else {
            logMessage = e.toString();
        }

        try {
            if (requeueThis) {
                this.logger.logError(this.logFrom,
                        String.format("REQUEUE delete that failed on backend %s with error %s",
                                server.getServerIdentifier(), logMessage));
                // requeue
                this.database.queueUpdate(this.serverType, queued.getAiid(),
                        true, this.config.getProcessQueueScheduleFutureCommand(), QueueAction.DELETE);
            } else {
                // drop the delete command and ignore permanently
                this.logger.logError(this.logFrom,
                        String.format("DROP delete that failed on backend %s with error %s",
                                server.getServerIdentifier(), logMessage));
            }
        } catch (Database.DatabaseException e1) {
            this.logger.logException(this.logFrom, e1);
        }
    }

    /***
     * Deal with logging and requeuing or dropping when a train task fails
     * @param queued
     * @param server
     * @param e
     */
    private void handleTrainTaskFailure(final BackendEngineStatus queued, final ServerTracker server,
                                        final ServerConnector.AiServicesException e) {

        // queue flag
        boolean requeueThis = true;
        String logMessage;
        // get the first error with a non-zero result code
        ServerConnector.AiServicesException httpError = findFirstSuppressedHttpError(e);
        // if there is one
        if (httpError != null) {
            switch (httpError.getResponseStatus()) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                case HttpURLConnection.HTTP_NOT_FOUND:
                    requeueThis = false;
                    break;
                default:
                    break;
            }
            logMessage = httpError.getMessage();
        } else {
            logMessage = e.toString();
        }

        try {
            if (requeueThis) {
                this.logger.logError(this.logFrom,
                        String.format("REQUEUE train task that failed on backend %s with error %s",
                                server.getServerIdentifier(), logMessage));
                // requeue the task now
                this.database.queueUpdate(this.serverType, queued.getAiid(),
                        true, this.config.getProcessQueueScheduleFutureCommand(), QueueAction.TRAIN);
            } else {
                // don't requeue
                this.logger.logError(this.logFrom,
                        String.format("DROP train-task that failed on backend %s with error %s",
                                server.getServerIdentifier(), logMessage));
                // and permanently flag the AI state as ERROR
                this.database.updateAIStatus(this.serverType, queued.getAiid(), TrainingStatus.AI_ERROR,
                        server.getServerIdentifier(), 0.0, 0.0);
            }
        } catch (Database.DatabaseException e1) {
            this.logger.logException(this.logFrom, e1);
        }
    }

    /***
     * Process a deletion
     * @param queued
     * @param server
     */
    protected void unqueueDelete(final BackendEngineStatus queued, final ServerTracker server) {

        // read the queue status of this ai
        BackendEngineStatus currentStatus = null;
        try {
            currentStatus = this.database.getAiQueueStatus(this.serverType, queued.getAiid());
        } catch (Database.DatabaseException e) {
            this.logger.logException(this.logFrom, e);
        }

        try {
            // if we have a status then proceed to tell the backend
            // to delete this AI
            if (currentStatus != null) {
                this.queueServices.deleteAIDirect(currentStatus.getDevId(), queued.getAiid(),
                        server.getServerUrl(), server.getServerIdentifier());
            } else {
                this.logger.logDebug(this.logFrom, "missing parent AI; cannot delete AI on backend");
            }

            // delete the status for this AI
            this.database.deleteAiStatus(this.serverType, queued.getAiid());

        } catch (ServerConnector.AiServicesException e) {
            handleDeleteTaskFailure(queued, server, e);
        } catch (Database.DatabaseException e) {
            this.logger.logError(this.logFrom,
                    String.format("db exception while deleting AI status: %s", e.toString()));
        }
    }

    /***
     * Process a training task
     * @param queued
     * @param server
     */
    protected void unqueueTrain(final BackendEngineStatus queued, final ServerTracker server) {

        // read the queue status of this ai
        BackendEngineStatus currentStatus = null;
        try {
            currentStatus = this.database.getAiQueueStatus(this.serverType, queued.getAiid());
        } catch (Database.DatabaseException e) {
            this.logger.logException(this.logFrom, e);
        }

        // if there is a status and the AI was not deleted (shouldn't happen)
        if ((currentStatus == null) || (currentStatus.isDeleted())) {
            this.logger.logError(this.logFrom,
                    String.format("trying to train but bot %s has been deleted", queued.getAiid().toString()));
            return;
        }

        try {
            // tell the chosen server to start training
            this.queueServices.startTrainingDirect(currentStatus.getDevId(), queued.getAiid(),
                    server.getServerUrl(), server.getServerIdentifier());
            // carefully set the AI status to indicate that the AI is training and the
            // slot is in use
            // when training actually starts the server will call us back to set status
            this.database.updateAIStatus(this.serverType, queued.getAiid(),
                    TrainingStatus.AI_TRAINING, server.getServerIdentifier(),
                    currentStatus.getTrainingProgress(), currentStatus.getTrainingError());
        } catch (ServerConnector.AiServicesException e) {
            handleTrainTaskFailure(queued, server, e);
        } catch (Database.DatabaseException e) {
            this.logger.logError(this.logFrom, String.format("failed to set endpoint in status: %s", e.toString()));
        }
    }

    /***
     * Check the queue state and run a task if there is one
     * @throws Database.DatabaseException
     */
    protected void processQueue() throws Database.DatabaseException {

        // get a summary of training slots from the database
        List<ServerEndpointTrainingSlots> slotList = this.database.getQueueSlotCounts(this.serverType);

        // if any training slots have timed out, requeue them
        // TODO: deal with slots that are in use but need to be requeued

        // make the slot summary into a map to prepare for matching
        Map<String, ServerEndpointTrainingSlots> slotLookup = slotList.stream()
                .collect(Collectors.toMap(ServerEndpointTrainingSlots::getEndpoint, Function.identity()));

        // get a map of connected endpoints
        Map<String, ServerTracker> serverMap = this.controller.getVerifiedEndpointMap();

        // for each connected server, set the capacity
        // and set the number of slots in use
        serverMap.forEach((name, tracker) -> {
            ServerEndpointTrainingSlots endpoint = slotLookup.computeIfAbsent(name,
                    key -> new ServerEndpointTrainingSlots(key, 0, 0));
            endpoint.setTrainingCapacity(tracker.getTrainingCapacity());
            endpoint.setChatCapacity(tracker.getChatCapacity());
        });

        // count slots
        int availableTrainingSlots = 0;
        int totalTrainingCapacity = 0;
        int totalChatCapacity = 0;
        int serverCount = 0;

        // count available, capacity and training-capable servers
        for (ServerEndpointTrainingSlots endpoint : slotLookup.values()) {
            availableTrainingSlots += endpoint.getAvailableSlotCount();
            totalTrainingCapacity += endpoint.getTrainingCapacity();
            totalChatCapacity += endpoint.getChatCapacity();
            serverCount++;
        }

        storeServerStats(serverCount, totalTrainingCapacity, availableTrainingSlots, totalChatCapacity);

        // if there are no available slots
        if (availableTrainingSlots < 1) {
            // if there was no capacity to begin with
            queueCheckAgainAfter(this.config.getProcessQueueIntervalLong());
            return;
        }

        // filter out the server with the lowest load

        // TODO: consider picking a random available slot in the future
        // because the server with the lowest load might be faulty in some way
        // and we would keep assigning all tasks to the faulty server because
        // it will always have the lowest load
        ServerEndpointTrainingSlots lowestLoad = slotLookup.values().stream()
                .filter(ServerEndpointTrainingSlots::hasFreeTrainingSlots)
                .min(Comparator.comparing(ServerEndpointTrainingSlots::getLoadFactor))
                .orElse(null);

        // get a tracker for the server we have chosen
        ServerTracker chosenServer = serverMap.get(lowestLoad.getEndpoint());

        // take the next task off the queue
        BackendEngineStatus queued = this.database.queueTakeNext(this.serverType);

        // if there was a queued task then service the queue
        if (queued != null) {
            switch (queued.getQueueAction()) {
                case DELETE:
                    unqueueDelete(queued, chosenServer);
                    queueCheckAgainAfter(this.config.getProcessQueueIntervalShort());
                    break;
                case TRAIN:
                    unqueueTrain(queued, chosenServer);
                    queueCheckAgainAfter(this.config.getProcessQueueIntervalShort());
                    break;
                default:
            }
        } else {
            queueCheckAgainAfter(this.config.getProcessQueueIntervalLong());
        }
    }
}
