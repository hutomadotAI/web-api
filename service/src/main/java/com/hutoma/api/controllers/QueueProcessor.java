package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
                            (sinceLastRun < sinceLastKick) ? "" :
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

            // if there was a failure to connect with the backend
            this.logger.logError(this.logFrom,
                    String.format("requeuing due to failure to delete AI on backend %s", e.toString()));

            // requeue the deletion for later
            // TODO: distinguish between different errors
            // TODO: 500, 429 = requeue, 404 = don't requeue
            try {
                this.database.queueUpdate(this.serverType, queued.getAiid(),
                        true, this.config.getProcessQueueScheduleFutureCommand(), QueueAction.DELETE);
            } catch (Database.DatabaseException e1) {
                this.logger.logError(this.logFrom,
                        String.format("failed to requeue AI deletion on backend: %s", e1.toString()));
            }
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
            this.logger.logError(this.logFrom, String.format(
                    "requeuing due to failure to start training on backend %s", e.toString()));

            // TODO: distinguish between different errors
            // TODO: 500, 429 = requeue, 400, 404 = don't requeue
            // if we failed to talk to the back-end server then requeue this command to try again in 30 seconds
            try {
                this.database.queueUpdate(this.serverType, queued.getAiid(),
                        true, this.config.getProcessQueueScheduleFutureCommand(), QueueAction.TRAIN);
            } catch (Database.DatabaseException e1) {
                this.logger.logError(this.logFrom,
                        String.format("failed to requeue AI training on backend: %s", e1.toString()));
            }
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
        Map<String, ServerTracker> serverMap = this.controller.getEndpointTrainingMap();

        // for each connected server, set the capacity
        // and set the number of slots in use
        serverMap.forEach((name, tracker) -> {
            ServerEndpointTrainingSlots endpoint = slotLookup.computeIfAbsent(name,
                    key -> new ServerEndpointTrainingSlots(key, 0, 0));
            endpoint.setTrainingCapacity(tracker.getTrainingCapacity());
        });

        // count slots
        int availableSlots = 0;
        int totalCapacity = 0;
        int serverCount = 0;

        // count available, capacity and training-capable servers
        for (ServerEndpointTrainingSlots endpoint : slotLookup.values()) {
            availableSlots += endpoint.getAvailableSlotCount();
            totalCapacity += endpoint.getTrainingCapacity();
            serverCount += (endpoint.getTrainingCapacity() > 0) ? 1 : 0;
        }

        // if there are no available slots
        if (availableSlots < 1) {
            // if there was no capacity to begin with
            if (totalCapacity < 1) {
                // log it
                this.logger.logInfo(this.logFrom, "training capacity is zero");
            } else {
                // we're at capacity, so log the stats
                this.logger.logInfo(this.logFrom,
                        String.format("max training capacity: %d training slot%s on %d server%s in use",
                                totalCapacity, totalCapacity == 1 ? "" : "s",
                                serverCount, serverCount == 1 ? "" : "s"));
            }
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
