package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ServerRegistration;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/***
 * Code common to all controllers.
 * Singleton class that keeps track of registered back-end servers and their affinity
 */
public abstract class ControllerBase {

    private static final String LOGFROM = "controller";

    protected Config config;
    protected Tools tools;
    protected ThreadSubPool threadSubPool;
    protected ServiceLocator serviceLocator;
    protected ILogger logger;

    protected ConcurrentHashMap<UUID, ServerTracker> serverSessions;

    public ControllerBase(final Config config, final Tools tools, final ThreadSubPool threadSubPool,
                          final ServiceLocator serviceLocator,
                          final ILogger logger) {
        this.config = config;
        this.tools = tools;
        this.threadSubPool = threadSubPool;
        this.serviceLocator = serviceLocator;
        this.logger = logger;
        this.serverSessions = new ConcurrentHashMap<>();
    }

    public UUID registerServer(final ServerRegistration registration) {
        ServerTracker tracker = createNewServerTracker();
        UUID serverSessionID = tracker.trackServer(registration);

        // check any other valid sessions
        this.serverSessions.values().stream()
                // to see if any of them have the same callback URL
                .filter(oldTracker -> oldTracker.getServerUrl().equals(tracker.getServerUrl()))
                // and if they do, terminate them.
                .forEach(ServerTracker::endServerSession);

        // for this phase we are only accepting one server of each type
        // so close any other open session
        // begin temporary code
        this.serverSessions.values().stream()
                // and if they do, terminate them.
                .forEach(ServerTracker::endServerSession);
        // end temporary code

        // add this session to the list
        this.serverSessions.put(serverSessionID, tracker);

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
            this.serverSessions.remove(serverSessionID);
            this.logger.logInfo(LOGFROM, String.format("Dropped %s", tracker.describeServer()));
        });

        return serverSessionID;
    }

    protected ServerTracker createNewServerTracker() {
        return this.serviceLocator.getService(ServerTracker.class);
    }

    protected List<String> getBackendEndpoints() {
        // make a list with only the first entry (phase 1 only)
        Optional<ServerTracker> tracker = this.serverSessions.values().stream().findFirst();
        return tracker.isPresent() ?
                Collections.singletonList(tracker.get().getServerUrl()) : getFallbackBackendEndpoints();
    }

    protected abstract List<String> getFallbackBackendEndpoints();

}
