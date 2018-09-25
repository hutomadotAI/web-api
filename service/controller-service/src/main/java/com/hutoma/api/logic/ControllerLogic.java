package com.hutoma.api.logic;

import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerTrackerInfo;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.ServerEndpointRequestMulti;
import com.hutoma.api.controllers.ControllerBase;
import com.hutoma.api.controllers.ControllerMap;
import com.hutoma.api.controllers.ServerTracker;

import javax.inject.Inject;
import java.util.*;

public class ControllerLogic {

    private final ControllerMap controllerMap;

    @Inject
    public ControllerLogic(final ControllerMap controllerMap) {
        this.controllerMap = controllerMap;
    }

    public ApiResult getMap(final ServiceIdentity serviceIdentity) {
        Map<String, ServerTracker> trackerMap = this.controllerMap.getControllerFor(serviceIdentity)
                .getVerifiedEndpointMap();
        Map<String, ServerTrackerInfo> trackerInfoMap = getTrackerInfoFromMap(trackerMap);
        return new ApiServerTrackerInfoMap(trackerInfoMap).setSuccessStatus();
    }

    public ApiResult getBackendTrainingEndpoint(final UUID aiid,
                                                final ServiceIdentity serviceIdentity) {
        try {
            IServerEndpoint endpoint = this.controllerMap.getControllerFor(serviceIdentity)
                    .getUploadBackendEndpoint(aiid);
            return new ApiServerEndpoint(endpoint).setSuccessStatus();
        } catch (NoServerAvailableException ex) {
            return ApiError.getNotFound();
        } catch (Exception ex) {
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult kickQueue(final ServiceIdentity serviceIdentity) {
        try {
            this.controllerMap.getControllerFor(serviceIdentity).kickQueue();
            return new ApiResult().setSuccessStatus();
        } catch (Exception ex) {
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getBackendChatEndpointsMulti(final ServiceIdentity serviceIdentity,
                                                  final ServerEndpointRequestMulti serverEndpointRequestMulti) {

        // results
        List<ApiServerEndpointMulti.ServerEndpointResponse> results = new ArrayList<>();

        // get the controller
        ControllerBase controller = this.controllerMap.getControllerFor(serviceIdentity);

        // for every server requested (typically one main bot + one for every linked bot)
        for (ServerEndpointRequestMulti.ServerEndpointRequest request :
                serverEndpointRequestMulti.getEndpointRequests()) {
            try {
                // get the server to send the chat request to
                IServerEndpoint server = controller
                        .getChatBackendEndpoint(request.getAiid(), request.getAlreadyTried());
                // get the hash code
                String hash = controller.getHashCodeFor(request.getAiid());

                // combine all this into one
                results.add(new ApiServerEndpointMulti.ServerEndpointResponse(
                        request.getAiid(),
                        server.getServerUrl(), server.getServerIdentifier(),
                        hash));

            } catch (NoServerAvailableException noServer) {
                // return an empty container if no server is available
                results.add(new ApiServerEndpointMulti.ServerEndpointResponse(
                        request.getAiid(),
                        null, null, null));
            }
        }
        return new ApiServerEndpointMulti(results).setSuccessStatus();
    }

    public ApiResult getAllEndpoints() {
        Map<String, ServerTrackerInfo> trackerInfoMap = new HashMap<>();
        // We need to send a delete request for every controller we have currently registered
        Collection<ControllerBase> controllers = this.controllerMap.getAllDynamicControllers();
        for (ControllerBase controller : controllers) {
            Map<String, ServerTrackerInfo> info = getTrackerInfoFromMap(controller.getVerifiedEndpointMap());
            info.forEach(trackerInfoMap::put);
        }

        return new ApiServerTrackerInfoMap(trackerInfoMap).setSuccessStatus();
    }

    private static Map<String, ServerTrackerInfo> getTrackerInfoFromMap(final Map<String, ServerTracker> trackerMap) {
        Map<String, ServerTrackerInfo> trackerInfoMap = new HashMap<>();
        for (Map.Entry<String, ServerTracker> entry : trackerMap.entrySet()) {
            ServerTracker t = entry.getValue();
            ServerTrackerInfo info = new ServerTrackerInfo(t.getServerUrl(), t.getServerIdentifier(),
                    t.getChatCapacity(), t.getTrainingCapacity(), t.canTrain(), t.isEndpointVerified());
            trackerInfoMap.put(entry.getKey(), info);
        }
        return trackerInfoMap;
    }
}
