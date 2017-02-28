package com.hutoma.api.controllers;

import com.hutoma.api.common.AiServiceStatusLogger;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.containers.sub.ServerAiEntry;
import com.hutoma.api.containers.sub.ServerRegistration;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.HashMap;
import java.util.List;
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

    public ControllerBase(final ThreadSubPool threadSubPool,
                          final ServiceLocator serviceLocator,
                          final AiServiceStatusLogger logger) {
        super(logger);
        this.serviceLocator = serviceLocator;
        this.threadSubPool = threadSubPool;
        this.aiHashCodes = new HashMap<>();
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

    public String getBackendEndpoint(UUID aiid, RequestFor requestFor) throws NoServerAvailable {
        ServerTracker tracker = this.getServerFor(aiid, requestFor);
        return tracker.getServerUrl();
    }

    public synchronized String getHashCodeFor(UUID aiid) {
        return this.aiHashCodes.computeIfAbsent(aiid, v -> "");
    }

    public synchronized void setHashCodeFor(UUID aiid, String hashCode) {
        aiHashCodes.put(aiid, hashCode);
    }

    public synchronized void setAllHashCodes(final List<ServerAiEntry> aiList) {
        this.aiHashCodes.clear();
        aiList.stream().forEach(entry ->
                this.aiHashCodes.put(entry.getAiid(), entry.getAiHash()));
    }

    protected ServerTracker createNewServerTracker() {
        return this.serviceLocator.getService(ServerTracker.class);
    }

    public enum RequestFor {
        Training,
        Chat
    }
}
