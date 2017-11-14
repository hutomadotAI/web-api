package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.connectors.ServerTrackerInfo;

import java.util.Map;

public abstract class BackendServicesConnector {


    public void kickQueueProcessor() {
    }

    public abstract boolean logErrorIfNoTrainingCapacity();

    public abstract Map<String, ServerTrackerInfo> getVerifiedEndpointMap();
}
