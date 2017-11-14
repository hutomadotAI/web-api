package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.ServerTrackerInfo;

import java.util.Map;
import javax.inject.Inject;

public class RnnServicesConnector extends BackendServicesConnector{

    private final QueueProcessor queueProcessor;
    private final ControllerConnector controllerConnector;

    @Inject
    public RnnServicesConnector(final QueueProcessor queueProcessor, final ControllerConnector controllerConnector) {
        this.queueProcessor = queueProcessor;
        this.queueProcessor.initialise(this, BackendServerType.RNN);
        this.controllerConnector = controllerConnector;
    }

    @Override
    public void kickQueueProcessor() {
        this.queueProcessor.kickQueueProcessor();
    }

    @Override
    public boolean logErrorIfNoTrainingCapacity() {
        return true;
    }

    @Override
    public Map<String, ServerTrackerInfo> getVerifiedEndpointMap() {
        return controllerConnector.getVerifiedEndpointMap(BackendServerType.RNN);
    }
}
