package com.hutoma.api.logic;

import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.RequestFor;
import com.hutoma.api.connectors.ServerTrackerInfo;
import com.hutoma.api.containers.ApiError;
import com.hutoma.api.containers.ApiResult;
import com.hutoma.api.containers.ApiServerEndpoint;
import com.hutoma.api.containers.ApiServerHashcode;
import com.hutoma.api.containers.ApiServerTrackerInfoMap;
import com.hutoma.api.controllers.ControllerAiml;
import com.hutoma.api.controllers.ControllerBase;
import com.hutoma.api.controllers.ControllerRnn;
import com.hutoma.api.controllers.ControllerWnet;
import com.hutoma.api.controllers.ServerTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class ControllerLogic {

    private final Map<BackendServerType, ControllerBase> controllerMap = new HashMap<>();

    @Inject
    public ControllerLogic(final ControllerAiml controllerAiml, final ControllerWnet controllerWnet,
                           final ControllerRnn controllerRnn) {
        controllerMap.put(BackendServerType.AIML, controllerAiml);
        controllerMap.put(BackendServerType.WNET, controllerWnet);
        controllerMap.put(BackendServerType.RNN, controllerRnn);
    }

    public ApiResult getMap(final BackendServerType serverType) {
        Map<String, ServerTracker> trackerMap = this.controllerMap.get(serverType).getVerifiedEndpointMap();
        Map<String, ServerTrackerInfo> trackerInfoMap = new HashMap<>();
        for (Map.Entry<String, ServerTracker> entry: trackerMap.entrySet()) {
            ServerTracker t = entry.getValue();
            ServerTrackerInfo info = new ServerTrackerInfo(t.getServerUrl(), t.getServerIdentifier(),
                    t.getChatCapacity(), t.getTrainingCapacity(), t.canTrain(), t.isEndpointVerified());
            trackerInfoMap.put(entry.getKey(), info);
        }

        return new ApiServerTrackerInfoMap(trackerInfoMap).setSuccessStatus();
    }

    public ApiResult getBackendEndpoint(final UUID aiid, final RequestFor requestFor,
                                        final BackendServerType serverType) {
        try {
            IServerEndpoint endpoint = this.controllerMap.get(serverType).getBackendEndpoint(aiid, requestFor);
            return new ApiServerEndpoint(endpoint).setSuccessStatus();
        } catch (NoServerAvailableException ex) {
            return ApiError.getNotFound();
        } catch (Exception ex) {
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getHashCodeFor(final UUID aiid, final BackendServerType serverType) {
        try {
            String hash = this.controllerMap.get(serverType).getHashCodeFor(aiid);
            return new ApiServerHashcode(hash).setSuccessStatus();
        } catch (Exception ex) {
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult kickQueue(final BackendServerType serverType) {
        try {
            this.controllerMap.get(serverType).kickQueue();
            return new ApiResult().setSuccessStatus();
        } catch (Exception ex) {
            return ApiError.getInternalServerError();
        }
    }
}
