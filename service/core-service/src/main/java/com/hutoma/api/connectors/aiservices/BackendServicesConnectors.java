package com.hutoma.api.connectors.aiservices;

import com.hutoma.api.common.JsonSerializer;
import com.hutoma.api.connectors.*;
import com.hutoma.api.connectors.db.DatabaseException;
import com.hutoma.api.containers.sub.AiIdentity;
import com.hutoma.api.logging.ILogger;
import com.hutoma.api.logging.LogMap;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackendServicesConnectors {

    private static final String LOGFROM = "backendservicesconnectors";
    private final List<ConnectorItem> connectors = new ArrayList<>();

    @Inject
    BackendServicesConnectors(final EmbServicesConnector embServicesConnector,
                              final Doc2ChatServicesConnector doc2ChatServicesConnector) {
        this.connectors.add(new ConnectorItem(embServicesConnector, BackendServerType.EMB, false));
        this.connectors.add(new ConnectorItem(doc2ChatServicesConnector, BackendServerType.DOC2CHAT, true));
    }

    public void startTraining(final AiServicesQueue queueServices,
                              final BackendStatus status,
                              final AiIdentity aiIdentity)
            throws DatabaseException {
        for (ConnectorItem connectorItem : this.connectors) {
            queueServices.userActionStartTraining(status, connectorItem.serverType, aiIdentity);
            connectorItem.connector.kickQueueProcessor(aiIdentity.getLanguage(), aiIdentity.getServerVersion());
        }
    }

    public void stopTraining(final AiServicesQueue queueServices,
                             final BackendStatus backendStatus,
                             final AiIdentity aiIdentity)
            throws DatabaseException, ServerConnector.AiServicesException {
        for (ConnectorItem connectorItem : this.connectors) {
            queueServices.userActionStopTraining(backendStatus, connectorItem.serverType, connectorItem.connector,
                    aiIdentity);
        }
    }

    public void deleteAi(final AiServicesQueue queueServices,
                         final BackendStatus backendStatus,
                         final AiIdentity aiIdentity)
            throws DatabaseException, ServerConnector.AiServicesException {
        for (ConnectorItem connectorItem : this.connectors) {
            queueServices.userActionDelete(backendStatus, connectorItem.serverType, connectorItem.connector,
                    aiIdentity);
            connectorItem.connector.kickQueueProcessor(aiIdentity.getLanguage(), aiIdentity.getServerVersion());
        }
    }

    public List<String> getEndpointsForAllServerTypes(final JsonSerializer serializer) {
        List<String> list = new ArrayList<>();
        for (ConnectorItem connectorItem : this.connectors) {
            Map<String, ServerTrackerInfo> map = connectorItem.connector.getEndpointsForBroadcast(serializer);
            if (map != null) {
                map.values().forEach(x -> list.add(x.getServerUrl()));
            }
        }
        return list;
    }

    public void uploadTraining(final AiServicesQueue queueServices,
                               final BackendStatus backendStatus,
                               final AiIdentity aiIdentity)
            throws DatabaseException, ServerConnector.AiServicesException {
        for (ConnectorItem connectorItem : this.connectors) {
            queueServices.userActionUpload(backendStatus, connectorItem.serverType, connectorItem.connector,
                    aiIdentity);
        }
    }

    public List<String> getListOfPrimaryEndpoints(final AiIdentity aiIdentity,
                                                  final JsonSerializer serializer,
                                                  final ILogger logger)
            throws NoServerAvailableException {
        List<String> endpoints = new ArrayList<>();
        for (ConnectorItem connectorItem : this.connectors) {
            try {
                endpoints.add(connectorItem.connector.getBackendTrainingEndpoint(aiIdentity, serializer)
                        .getServerUrl());
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
