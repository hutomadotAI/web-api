package com.hutoma.api.controllers;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.common.Tools;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.IConnectConfig;
import com.hutoma.api.connectors.InvocationResult;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.db.DatabaseAiStatusUpdates;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.sub.DevPlan;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;
import com.hutoma.api.thread.TrackedThreadSubPool;

import org.glassfish.jersey.client.JerseyClient;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.inject.Inject;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

public class AIQueueServices extends ServerConnector {

    private static final String LOGFROM = "aiqueueservices";
    private static final String COMMAND_PARAM = "command";
    private static final String TRAINING_TIME_ALLOWED_PARAM = "training_time_allowed";

    private final DatabaseAiStatusUpdates databaseAiStatusUpdates;

    @Inject
    public AIQueueServices(final DatabaseAiStatusUpdates databaseAiStatusUpdates, final ILogger logger,
                           final IConnectConfig connectConfig,
                           final JsonSerializer serializer, final Tools tools,
                           final JerseyClient jerseyClient, final TrackedThreadSubPool threadSubPool) {
        super(logger, connectConfig, serializer, tools, jerseyClient, threadSubPool);
        this.databaseAiStatusUpdates = databaseAiStatusUpdates;
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
                        .target(endpoint)
                        .path(devIdString)
                        .path(aiid.toString())
                        .property(CONNECT_TIMEOUT, (int) this.connectConfig.getBackendConnectCallTimeoutMs())
                        .property(READ_TIMEOUT, (int) this.connectConfig.getBackendTrainingCallTimeoutMs())
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
            devPlan = this.databaseAiStatusUpdates.getDevPlan(devId);
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


}

