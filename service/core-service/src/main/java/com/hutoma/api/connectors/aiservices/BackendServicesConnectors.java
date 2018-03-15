package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.BackendServerType;
import com.hutoma.api.connectors.BackendStatus;
import com.hutoma.api.connectors.NoServerAvailableException;
import com.hutoma.api.connectors.ServerConnector;
import com.hutoma.api.connectors.ServerTrackerInfo;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;

public class BackendServicesConnectors {

    private static final String LOGFROM = "backendservicesconnectors";
    private final List<ConnectorItem> connectors = new ArrayList<>();

    @Inject
    BackendServicesConnectors(final WnetServicesConnector wnetServicesConnector,
                              final SvmServicesConnector svmServicesConnector,
                              final EmbServicesConnector embServicesConnector) {
        connectors.add(new ConnectorItem(wnetServicesConnector, BackendServerType.WNET, false));
        connectors.add(new ConnectorItem(svmServicesConnector, BackendServerType.SVM, true));
        connectors.add(new ConnectorItem(embServicesConnector, BackendServerType.EMB, true));
    }

    public void startTraining(final AiServicesQueue queueServices, final BackendStatus status,
                              final UUID devId, final UUID aiid)
            throws DatabaseException {
        for (ConnectorItem connectorItem: connectors) {
            queueServices.userActionStartTraining(status, connectorItem.serverType, devId, aiid);
            connectorItem.connector.kickQueueProcessor();
        }
    }

    public void stopTraining(final AiServicesQueue queueServices, final BackendStatus backendStatus,
                             final UUID devId, final UUID aiid)
            throws DatabaseException, ServerConnector.AiServicesException {
        for (ConnectorItem connectorItem: connectors) {
            queueServices.userActionStopTraining(backendStatus, connectorItem.serverType, connectorItem.connector,
                    devId, aiid);
        }
    }

    public void deleteAi(final AiServicesQueue queueServices, final BackendStatus backendStatus,
                         final UUID devId, final UUID aiid)
            throws DatabaseException, ServerConnector.AiServicesException {
        for (ConnectorItem connectorItem: connectors) {
            queueServices.userActionDelete(backendStatus, connectorItem.serverType, connectorItem.connector,
                    devId, aiid);
            connectorItem.connector.kickQueueProcessor();
        }
    }

    public List<String> getEndpointsForAllServerTypes(final JsonSerializer serializer) {
        List<String> list = new ArrayList<>();
        for (ConnectorItem connectorItem: connectors) {
            Optional<ServerTrackerInfo> info = connectorItem.connector
                    .getVerifiedEndpointMap(serializer)
                    .values()
                    .stream()
                    .findFirst();
            info.ifPresent(serverTrackerInfo -> list.add(serverTrackerInfo.getServerUrl()));
        }
        return list;
    }

    public void uploadTraining(final AiServicesQueue queueServices, final BackendStatus backendStatus,
                               final UUID devId, final UUID aiid)
            throws DatabaseException, ServerConnector.AiServicesException {
        for (ConnectorItem connectorItem: connectors) {
            queueServices.userActionUpload(backendStatus, connectorItem.serverType, connectorItem.connector,
                    devId, aiid);
        }
    }

    public List<String> getListOfPrimaryEndpoints(final UUID aiid, final JsonSerializer serializer,
                                                  final ILogger logger)
            throws NoServerAvailableException {
        List<String> endpoints = new ArrayList<>();
        for (ConnectorItem connectorItem: connectors) {
            try {
                endpoints.add(connectorItem.connector.getBackendTrainingEndpoint(aiid, serializer).getServerUrl());
            } catch (Exception ex) {
                if (connectorItem.isShadow) {
                    // Ignore any exceptions when obtaining the endpoint for a shadow service
                    logger.logDebug(LOGFROM, String.format("Exception when obtaining %s training endpoint",
                            connectorItem.serverType.value()),
                            LogMap.map("Message", ex.getMessage()));
                } else {
                    throw ex;
                }

            }
        }
        return endpoints;
    }

    private static class ConnectorItem {
        private ControllerConnector connector;
        private boolean isShadow;
        private BackendServerType serverType;

        ConnectorItem(final ControllerConnector connector, final BackendServerType serverType,
                      final boolean isShadow) {
            this.connector = connector;
            this.serverType = serverType;
            this.isShadow = isShadow;
        }
    }
}
