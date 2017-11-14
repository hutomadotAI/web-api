package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.connectors.ServerTrackerInfo;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerBase;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.controllers.ServerTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class ControllerConnector {

    // Use the controllers for now until this is actually a separate service
    private final Map<BackendServerType, ControllerBase> controllerMap = new HashMap<>();

    @Inject
    public ControllerConnector(final ControllerAiml controllerAiml, final ControllerWnet controllerWnet,
                               final ControllerRnn controllerRnn) {
        controllerMap.put(BackendServerType.AIML, controllerAiml);
        controllerMap.put(BackendServerType.WNET, controllerWnet);
        controllerMap.put(BackendServerType.RNN, controllerRnn);
    }

    public IServerEndpoint getBackendEndpoint(final UUID aiid, final RequestFor requestFor,
                                              final BackendServerType serverType)
            throws NoServerAvailableException {
        return this.controllerMap.get(serverType).getBackendEndpoint(aiid, requestFor);
    }

    public String getHashCodeFor(final UUID aiid, final BackendServerType serverType) {
        return this.controllerMap.get(serverType).getHashCodeFor(aiid);
    }

    public Map<String, ServerTrackerInfo> getVerifiedEndpointMap(final BackendServerType serverType) {
        Map<String, ServerTracker> trackerMap = this.controllerMap.get(serverType).getVerifiedEndpointMap();
        Map<String, ServerTrackerInfo> trackerInfoMap = new HashMap<>();
        for (Map.Entry<String, ServerTracker> entry: trackerMap.entrySet()) {
            ServerTracker t = entry.getValue();
            ServerTrackerInfo info = new ServerTrackerInfo(t.getServerUrl(), t.getServerIdentifier(),
                    t.getChatCapacity(), t.getTrainingCapacity(), t.canTrain(), t.isEndpointVerified());
            trackerInfoMap.put(entry.getKey(), info);
        }
        return trackerInfoMap;
    }
}
