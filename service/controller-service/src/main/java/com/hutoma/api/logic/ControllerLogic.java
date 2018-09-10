package com.hutoma.api.logic;

import com.hutoma.api.common.SupportedLanguage;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.IServerEndpoint;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerTrackerInfo;
import com.hutoma.api.containers.*;
import com.hutoma.api.containers.sub.ServerEndpointRequestMulti;
import com.hutoma.api.controllers.ControllerBase;
import com.hutoma.api.controllers.ControllerMap;
import com.hutoma.api.controllers.ServerTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

public class ControllerLogic {

    private final ControllerMap controllerMap;

    @Inject
    public ControllerLogic(final ControllerMap controllerMap) {
        this.controllerMap = controllerMap;
    }

    public ApiResult getMap(final BackendServerType serverType,
                            final SupportedLanguage serverLanguage,
                            final String serverVersion) {
        Map<String, ServerTracker> trackerMap = this.controllerMap.getControllerFor(
                new ServiceIdentity(serverType, serverLanguage, serverVersion))
                .getVerifiedEndpointMap();
        Map<String, ServerTrackerInfo> trackerInfoMap = new HashMap<>();
        for (Map.Entry<String, ServerTracker> entry : trackerMap.entrySet()) {
            ServerTracker t = entry.getValue();
            ServerTrackerInfo info = new ServerTrackerInfo(t.getServerUrl(), t.getServerIdentifier(),
                    t.getChatCapacity(), t.getTrainingCapacity(), t.canTrain(), t.isEndpointVerified());
            trackerInfoMap.put(entry.getKey(), info);
        }

        return new ApiServerTrackerInfoMap(trackerInfoMap).setSuccessStatus();
    }

    public ApiResult getBackendTrainingEndpoint(final UUID aiid,
                                                final BackendServerType serverType,
                                                final SupportedLanguage serverLanguage,
                                                final String serverVersion) {
        try {
            IServerEndpoint endpoint = this.controllerMap.getControllerFor(
                    new ServiceIdentity(serverType, serverLanguage, serverVersion))
                    .getUploadBackendEndpoint(aiid);
            return new ApiServerEndpoint(endpoint).setSuccessStatus();
        } catch (NoServerAvailableException ex) {
            return ApiError.getNotFound();
        } catch (Exception ex) {
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult kickQueue(final BackendServerType serverType,
                               final SupportedLanguage serverLanguage,
                               final String serverVersion) {
        try {
            this.controllerMap.getControllerFor(
                    new ServiceIdentity(serverType, serverLanguage, serverVersion)).kickQueue();
            return new ApiResult().setSuccessStatus();
        } catch (Exception ex) {
            return ApiError.getInternalServerError();
        }
    }

    public ApiResult getBackendChatEndpointsMulti(final BackendServerType backendServerType,
                                                  final SupportedLanguage backendServerLanguage,
                                                  final String backendServerVersion,
                                                  final ServerEndpointRequestMulti serverEndpointRequestMulti) {

        // results
        List<ApiServerEndpointMulti.ServerEndpointResponse> results = new ArrayList<>();

        // get the controller
        ControllerBase controller = controllerMap.getControllerFor(
                new ServiceIdentity(backendServerType, backendServerLanguage, backendServerVersion));

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

}
