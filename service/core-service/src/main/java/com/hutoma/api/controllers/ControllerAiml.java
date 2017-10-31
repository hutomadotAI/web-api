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

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * Created by David MG on 01/02/2017.
 */
public class ControllerAiml extends ControllerBase {

    @Inject
    public ControllerAiml(final Config config, final ThreadSubPool threadSubPool,
                          final ServiceLocator serviceLocator, final AiServiceStatusLogger logger,
                          final QueueProcessor queueProcessor) {
        super(config, threadSubPool, serviceLocator, logger);
        this.queueProcessor = queueProcessor;
        this.queueProcessor.initialise(this, BackendServerType.AIML);
    }

    /***
     * When a server registers take its server list and
     * register affinity for all the AIs listed
     * @param registration reg data including list of AIs
     * @return session ID
     */
    @Override
    public UUID registerServer(final ServerRegistration registration) {
        UUID sessionID = super.registerServer(registration);
        this.updateAffinity(sessionID,
                registration.getAiList().stream()
                        .map(ServerAiEntry::getAiid)
                        .collect(Collectors.toList()));
        return sessionID;
    }

    @Override
    public void synchroniseDBStatuses(final DatabaseAiStatusUpdates database,
                                      final JsonSerializer jsonSerializer, final BackendServerType serverType,
                                      final Map<UUID, ServerAiEntry> statusData)
            throws DatabaseException {
        // do nothing.
        // AIML AIs are placeholders and should not be synced
    }

    @Override
    public boolean logErrorIfNoTrainingCapacity() {
        return false;
    }

    /***
     * Override the ability to assign a server if there is no existing affinity
     * So a chat request will throw a "noserver" exception unless a server has
     * explicitly registered to service this AIID
     * @param aiid
     * @return never returns
     * @throws NoServerAvailable
     */
    @Override
    protected synchronized ServerTracker chooseServerToAssignAffinity(final UUID aiid) throws NoServerAvailable {
        throw new NoServerAvailable(
                String.format("no AIML server registered to service AIML aiid %s", aiid.toString()));
    }
}
