package com.hutoma.api.controllers;

import com.hutoma.api.common.Config;
import com.hutoma.api.common.ILogger;
import com.hutoma.api.common.ThreadSubPool;
import com.hutoma.api.common.Tools;
import com.hutoma.api.containers.sub.ServerRegistration;

import org.glassfish.hk2.api.ServiceLocator;

import java.util.UUID;


/***
 * Code common to all controllers.
 * Singleton class that keeps track of registered back-end servers and their affinity
 */
public abstract class ControllerBase extends ServerMetadata {

    private static final String LOGFROM = "controller";

    protected ThreadSubPool threadSubPool;

    public ControllerBase(final Config config, final Tools tools, final ThreadSubPool threadSubPool,
                          final ServiceLocator serviceLocator,
                          final ILogger logger) {
        super(logger, config, tools, serviceLocator);
        this.threadSubPool = threadSubPool;
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

    public enum RequestFor {
        Training,
        Chat
    }
}
