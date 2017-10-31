package com.hutoma.api.controllers;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.Config;
import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.thread.ThreadSubPool;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.sub.BackendServerType;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.ServerRegistration;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/***
 * Code common to all controllers.
 * Singleton class that keeps track of registered back-end servers and their affinity
 */
public abstract class ControllerBase extends ServerMetadata {

    private static final String LOGFROM = "controller";
    protected final HashMap<UUID, String> aiHashCodes;
    protected ThreadSubPool threadSubPool;
    protected ServiceLocator serviceLocator;
    protected HashSet<UUID> botExclusionList;
    protected QueueProcessor queueProcessor;
    Config config;

    public ControllerBase(final Config config,
                          final ThreadSubPool threadSubPool,
                          final ServiceLocator serviceLocator,
                          final AiServiceStatusLogger logger) {
        super(logger);
        this.config = config;
        this.serviceLocator = serviceLocator;
        this.threadSubPool = threadSubPool;
        this.aiHashCodes = new HashMap<>();
        this.botExclusionList = new HashSet<>(config.getAimlBotAiids());
    }

    public UUID registerServer(final ServerRegistration registration) {
        ServerTracker tracker = createNewServerTracker();
        UUID serverSessionID = tracker.trackServer(registration);
        addNewSession(serverSessionID, tracker);

        // run a monitoring thread
        this.threadSubPool.submit(() -> {
            this.logger.logInfo(LOGFROM, String.format("Registered %s", tracker.describeServer()));
            // run the tracker thread inside
            try {
                // if this ends for any reason
                this.threadSubPool.submit(tracker).get();
            } catch (Exception e) {
                this.logger.logException(LOGFROM, e);
            }
            // remove the server from our active list
            deleteSession(serverSessionID);
            this.logger.logInfo(LOGFROM, String.format("Dropped %s", tracker.describeServer()));
        });

        return serverSessionID;
    }

    public IServerEndpoint getBackendEndpoint(UUID aiid, RequestFor requestFor) throws NoServerAvailable {
        ServerTracker tracker = this.getServerFor(aiid, requestFor);
        return tracker;
    }

    public synchronized String getHashCodeFor(UUID aiid) {
        return this.aiHashCodes.computeIfAbsent(aiid, v -> "");
    }

    public synchronized void setHashCodeFor(UUID aiid, String hashCode) {
        this.aiHashCodes.put(aiid, hashCode);
    }

    public synchronized void setAllHashCodes(final List<ServerAiEntry> aiList) {
        this.aiHashCodes.clear();
        aiList.stream().forEach(entry ->
                this.aiHashCodes.put(entry.getAiid(), entry.getAiHash()));
    }

    /***
     * Controller function to pass backend reported AI list with statuses
     * to the database class to sync what is already in the DB
     * @param database need to pass an instance of the database because the controller is a singleton
     * @param jsonSerializer need to pass a serializer because the controller is a singleton
     * @param serverType what server is this?
     * @param statusData mapped data that the backe nd server has reported
     * @throws DatabaseException
     */
    public void synchroniseDBStatuses(final DatabaseAiStatusUpdates database,
                                      final JsonSerializer jsonSerializer,
                                      final BackendServerType serverType,
                                      final Map<UUID, ServerAiEntry> statusData) throws DatabaseException {
        database.synchroniseDBStatuses(jsonSerializer, serverType, statusData, this.botExclusionList);
    }

    /***
     * If something has changed in the queue and we would like
     * the queue processor to run sooner rather than later
     */
    public void kickQueueProcessor() {
    }

    /***
     * Does this flavour of server require training capacity to operate?
     * @return
     */
    public abstract boolean logErrorIfNoTrainingCapacity();

    public enum RequestFor {
        Training,
        Chat
    }

    protected ServerTracker createNewServerTracker() {
        return this.serviceLocator.getService(ServerTracker.class);
    }
}
